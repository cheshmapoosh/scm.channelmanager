package ir.daneshrefah.scm.plugin.camel.component.tcp.nab.atps;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Decoder for a stream like:
 *   [len(5 ASCII digits)][data] [len(5)][data] ...
 *
 * Features:
 *  - Supports length that is either body-only or includes the 5-byte length field itself.
 *  - Optionally aggregates frames until a terminator frame appears (exact body match).
 *  - Protects against OOM via maxTotalBytes.
 *
 * Notes:
 *  - Emits either a single aggregated byte[] (when terminator is used and reached), or
 *    continuously emits individual frames if no terminator is configured.
 *  - If you need “batch at idle” behavior, keep this decoder frame-based and add an
 *    IdleStateHandler + a custom flush handler in the pipeline (outside this class).
 */
public final class AtpsResponseBodyDecoder extends ByteToMessageDecoder {

    public static final byte[] TERMINATOR_BYTES = {'0'};
    /** Maximum total bytes allowed for aggregation/output to avoid OOM. */
    private final int maxTotalBytes;

    /** If true, the parsed length includes the 5-byte ASCII length field itself. */
    private final boolean lengthIncludesSelf;

    /**
     * Optional terminator body (exact match). If non-null, decoder aggregates all frames into a single
     * byte[] and emits it once a frame equals this terminator. If null, frames are emitted one-by-one.
     */
    private final byte[] terminatorBytes;

    /** Internal aggregator used only when terminatorBytes != null. */
    private ByteArrayOutputStream aggregate;

    public AtpsResponseBodyDecoder(int maxTotalBytes) {
        this(maxTotalBytes, false, TERMINATOR_BYTES);
    }

    public AtpsResponseBodyDecoder(int maxTotalBytes, boolean lengthIncludesSelf) {
        this(maxTotalBytes, lengthIncludesSelf, TERMINATOR_BYTES);
    }

    public AtpsResponseBodyDecoder(int maxTotalBytes, boolean lengthIncludesSelf, byte[] terminatorBytes) {
        if (maxTotalBytes <= 5) throw new IllegalArgumentException("maxTotalBytes must be > 5");
        this.maxTotalBytes = maxTotalBytes;
        this.lengthIncludesSelf = lengthIncludesSelf;
        this.terminatorBytes = (terminatorBytes == null || terminatorBytes.length == 0) ? null : terminatorBytes.clone();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        for (;;) {
            if (in.readableBytes() < 5) return;               // need full 5-digit length

            in.markReaderIndex();
            final int len = readLen5Ascii(in, ctx);
            if (len < 0) return;                               // error already handled/closed

            final int bodyLen = lengthIncludesSelf ? (len - 5) : len;
            if (bodyLen < 0) {
                fail(ctx, "Negative body length computed: " + bodyLen + " (len=" + len + ")");
                return;
            }

            if (in.readableBytes() < bodyLen) {
                in.resetReaderIndex();                         // wait for full body
                return;
            }

            final byte[] body = new byte[bodyLen];
            in.readBytes(body);

            // Mode 1: no terminator => emit each frame immediately
            if (terminatorBytes == null) {
                out.add(body);
                continue;
            }

            // Mode 2: terminator present => aggregate until hit
            if (aggregate == null) {
                aggregate = new ByteArrayOutputStream(Math.min(bodyLen + 1024, Math.max(bodyLen, 4096)));
            }

            // Guard against OOM
            if ((long) aggregate.size() + bodyLen > maxTotalBytes) {
                fail(ctx, "Aggregated response exceeds maxTotalBytes=" + maxTotalBytes);
                return;
            }

            // If this frame equals the terminator, emit aggregate (without appending the terminator)
            if (startsWith(body, terminatorBytes)) {
                if (aggregate.size() == 0) {
                    aggregate.write(body, 0, body.length);
                }
                final byte[] result = aggregate.toByteArray();
                aggregate = null;
                out.add(result);
                return; // deliver a single consolidated response
            }

            // Otherwise, keep aggregating
            aggregate.write(body, 0, body.length);
            aggregate.write('\n');
        }
    }

    @Override
    protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Try to consume any remaining complete frame(s)
        if (in.isReadable()) {
            decode(ctx, in, out);
        }

        // If channel is closing and we have an unfinished aggregate, emit it (optional design choice)
        if (aggregate != null && aggregate.size() > 0) {
            out.add(aggregate.toByteArray());
        }
        aggregate = null;

        super.decodeLast(ctx, in, out);
    }

    /** Parses exactly 5 ASCII digits as an integer length. On error, fires and closes channel. */
    private static int readLen5Ascii(ByteBuf in, ChannelHandlerContext ctx) {
        int len = 0;
        for (int i = 0; i < 5; i++) {
            final byte b = in.readByte();
            if (b < '0' || b > '9') {
                // No heavy String allocations in hot path; minimal context is enough
                fail(ctx, "Invalid length digit at pos " + i + ": byte=" + (b & 0xFF));
                return -1;
            }
            len = len * 10 + (b - '0');
        }
        return len;
    }

    private static void fail(ChannelHandlerContext ctx, String message) {
        ctx.fireExceptionCaught(new IllegalStateException(message));
        ctx.close();
    }

    private static boolean startsWith(byte[] array, byte[] prefix) {
        if (array == null || prefix == null) return false;
        if (prefix.length == 0 || array.length < prefix.length) return false;
        int mismatch = Arrays.mismatch(array, 0, prefix.length, prefix, 0, prefix.length);
        return mismatch == -1;
    }
}
