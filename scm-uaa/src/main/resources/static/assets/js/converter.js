const tablinks = document.getElementsByClassName("tab-links");
const tabcontents = document.getElementsByClassName("tab-contents");

const opentab = (tabname) => {
  [...tablinks].forEach((tablink) => {
    tablink.classList.remove("active-link");
  });
  [...tabcontents].forEach((tabcontent) => {
    tabcontent.classList.remove("active-tab");
  });
  event.currentTarget.classList.add("active-link");
  document.getElementById(tabname).classList.add("active-tab");
};

[...tablinks].forEach((tablink) => {
  tablink.addEventListener("click", (e) => opentab(e.target.dataset.id));
});