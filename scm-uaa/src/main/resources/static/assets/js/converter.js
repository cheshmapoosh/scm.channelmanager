const tablinks = document.getElementsByClassName("tab-links");
const tabcontents = document.getElementsByClassName("tab-contents");

const opentab = (tabname) => {
  console.log({ tabname });
  [...tablinks].forEach((tablink) => {
    tablink.classList.remove("active-link");
    if (tablink.getAttribute("data-id") === tabname) {
      tablink.classList.add("active-link");
    }
  });
  [...tabcontents].forEach((tabcontent) => {
    tabcontent.classList.remove("active-tab");
    if (tabcontent.getAttribute("id") === tabname) {
      tabcontent.classList.add("active-tab");
    }
  });
  localStorage.setItem("active-tab", tabname);
};
[...tablinks].forEach((tablink) => {
  tablink.addEventListener("click", (e) => opentab(e.target.dataset.id));
});
const initTabname = localStorage.getItem("active-tab") ?? "tab1";
opentab(initTabname);

function cancelConsent() {
  document.consent_form.reset();
  document.consent_form.submit();
}
