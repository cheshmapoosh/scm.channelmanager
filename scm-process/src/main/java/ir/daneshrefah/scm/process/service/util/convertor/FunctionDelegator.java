package ir.daneshrefah.scm.process.service.util.convertor;

public interface FunctionDelegator<I,O> {
    void init(I i);
    O apply(I i);
}
