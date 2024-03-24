const tablinks = document.getElementsByClassName("tab-links");
const tabcontents = document.getElementsByClassName("tab-contents");
const grantType = document.getElementById("grant_type");
const timeForm = document.getElementById("time-form");
const jwtForm = document.getElementById("jwt-form");
const base64Form = document.getElementById("base64-form");
const consentForm = document.getElementById("consent_form");
const usernameWrapper = document.getElementById("username_wrapper");
const passwordWrapper = document.getElementById("password_wrapper");
const clientIdWrapper = document.getElementById("client_id_wrapper");
const scopeWrapper = document.getElementById("scope_wrapper");
const registerCodeWrapper = document.getElementById("register_code_wrapper");
const claimCodeWrapper = document.getElementById("claim_code_wrapper");
const codeWrapper = document.getElementById("code_wrapper");
const redirectUriWrapper = document.getElementById("redirect_uri_wrapper");
const clientVersionWrapper = document.getElementById("client_version_wrapper");
const clientSignatureWrapper = document.getElementById(
  "client_signature_wrapper"
);
const accessParameterWrapper = document.getElementById(
  "access_parameter_wrapper"
);

const opentab = (tabname) => {
  fetch("/public/api/tools/clients", {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Network response was not ok");
        }
        return response.json();
      })
      .then((newUserData) => {
        console.log("New Client Data:", newUserData);
      })
      .catch((error) => {
        console.error("Error:", error);
      });
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

const itemList = [
  {
    el: usernameWrapper,
    showOn: [
      "first_password",
      "second_password",
      "authorization_code",
      "client_credentials",
    ],
  },
  {
    el: passwordWrapper,
    showOn: [
      "first_password",
      "second_password",
      "authorization_code",
      "client_credentials",
    ],
  },
  {
    el: clientIdWrapper,
    showOn: ["first_password", "second_password", "client_credentials"],
  },
  {
    el: scopeWrapper,
    showOn: ["first_password", "second_password", "client_credentials"],
  },
  {
    el: registerCodeWrapper,
    showOn: ["first_password", "second_password"],
  },
  {
    el: claimCodeWrapper,
    showOn: ["authorization_code"],
  },
  {
    el: codeWrapper,
    showOn: ["authorization_code"],
  },
  {
    el: redirectUriWrapper,
    showOn: ["authorization_code"],
  },
  {
    el: clientVersionWrapper,
    showOn: ["first_password", "second_password", "client_credentials"],
  },
  {
    el: clientSignatureWrapper,
    showOn: ["first_password", "second_password", "client_credentials"],
  },
  {
    el: accessParameterWrapper,
    showOn: ["first_password", "second_password", "client_credentials"],
  },
];
function handleGrantType(selected) {
  itemList.forEach((i) => {
    if (!i.showOn.includes(selected)) {
      i.el.style.display = "none";
    } else {
      i.el.style.display = "flex";
    }
  });
}

grantType.addEventListener("change", (e) => {
  handleGrantType(e.target.value);
});
handleGrantType("first_password");
const formList = [
  {
    formName: timeForm,
    action: function (e) {
      e.preventDefault();
      const data = Object.fromEntries(new FormData(timeForm));
      console.log(data);
      fetch("/public/tools/time", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error("Network response was not ok");
          }
          return response.json();
        })
        .then((newUserData) => {
          console.log("New User Data:", newUserData);
        })
        .catch((error) => {
          console.error("Error:", error);
        });
    },
  },
  {
    formName: jwtForm,
    action: function (e) {
      e.preventDefault();

      const data = Object.fromEntries(new FormData(jwtForm));
      fetch("/public/tools/jwt", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error("Network response was not ok");
          }
          return response.json();
        })
        .then((newUserData) => {
          console.log("New User Data:", newUserData);
        })
        .catch((error) => {
          console.error("Error:", error);
        });
    },
  },
  {
    formName: base64Form,
    action: function (e) {
      e.preventDefault();

      const data = Object.fromEntries(new FormData(base64Form));
      fetch("/public/tools/base64", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error("Network response was not ok");
          }
          return response.json();
        })
        .then((newUserData) => {
          console.log("New User Data:", newUserData);
        })
        .catch((error) => {
          console.error("Error:", error);
        });
    },
  },
  {
    formName: consentForm,
    action: function (e) {
      e.preventDefault();

      const formData = Object.fromEntries(new FormData(consentForm));
      const grantTypeValue = formData.grant_type;
      let data = {};
      if (grantTypeValue === "authorization_code") {
        data = {
          username: formData.username,
          password: formData.password,
          claim_code: formData.claim_code,
          code: formData.code,
          grant_type: formData.grant_type,
          redirect_uri: formData.redirect_uri,
        };
      }
      if (grantTypeValue === "first_password") {
        data = {
          access_parameter: formData.access_parameter,
          client_id: formData.client_id,
          client_signature: formData.client_signature,
          client_version: formData.client_version,
          grant_type: formData.grant_type,
          password: formData.password,
          register_code: formData.register_code,
          scope: formData.scope,
          username: formData.username,
        };
      }

      fetch("/oauth2/authorize", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error("Network response was not ok");
          }
          return response.json();
        })
        .then((newUserData) => {
          console.log("New User Data:", newUserData);
        })
        .catch((error) => {
          console.error("Error:", error);
        });
    },
  },
];
formList.forEach(({ formName, action }) =>
  formName.addEventListener("submit", action)
);

function isEmptyString(str) {
  if (str === null || str === undefined) {
    return true;
  }
  return str.trim() === "";
}

function isNotEmptyStr(str) {
  return !(str === null || str === undefined || str.trim() === "");
}
// ======================================================
// function cancelConsent() {
//   document.consent_form.reset();
//   document.consent_form.submit();
// }

// function convertTime() {
//   const url = "/public/api/tools/time";
//   const data = document.getElementById("inputLongTime").value;

//   const xhr = new XMLHttpRequest();
//   xhr.open("POST", url); // Set the URL from the form's action attribute
//   xhr.setRequestHeader("Content-Type", "text/plain"); // Set content type for string

//   xhr.onload = function () {
//     if (xhr.status === 200) {
//       document.getElementById("inputLongFormattedTime").value =
//         xhr.responseText;
//     } else {
//       document.getElementById("inputLongFormattedTime").value =
//         "Error: " + xhr.statusText;
//     }
//   };

//   xhr.onerror = function () {
//     document.getElementById("inputLongFormattedTime").value =
//       "Error making request";
//   };

//   xhr.send(data);
// }

// function convertLogin() {
//   const url = "/oauth2/token";
//   const grant_type = "first_password"; // || second_password
//   const username = "rezajms";
//   const password = "test";
//   const scope = ""; //"session";
//   const client_id = "ib";
//   const client_version = "";
//   const client_signature = "";
//   const register_code = "";
//   const access_parameter = "asdsad";
//   const claim_code = "456";
//   if (isEmptyString(username)) {
//     alert("invalid username");
//     return;
//   }
//   if (isEmptyString(password)) {
//     alert("invalid password");
//     return;
//   }
//   if (isEmptyString(client_id)) {
//     alert("invalid client_id");
//     return;
//   }
//   if (isEmptyString(access_parameter)) {
//     alert("invalid access_parameter");
//     return;
//   }
//   const params = new URLSearchParams();
//   params.append("grant_type", grant_type);
//   params.append("username", username);
//   params.append("password", password);
//   params.append("client_id", client_id);
//   params.append("access_parameter", access_parameter);
//   if (isNotEmptyStr(scope)) {
//     params.append("scope", scope);
//   }
//   if (isNotEmptyStr(client_version)) {
//     params.append("client_version", client_version);
//   }
//   if (isNotEmptyStr(client_signature)) {
//     params.append("client_signature", client_signature);
//   }
//   if (isNotEmptyStr(register_code)) {
//     params.append("register_code", register_code);
//   }
//   if (isNotEmptyStr(claim_code)) {
//     params.append("claim_code", claim_code);
//   }

//   const data = params.toString();

//   const xhr = new XMLHttpRequest();
//   xhr.open("POST", url); // Set the URL from the form's action attribute
//   xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded"); // Set content type for string

//   xhr.onload = function () {
//     if (xhr.status === 200) {
//       console.log("outputTokenBase64").value = xhr.responseText;
//     } else {
//       console.log("outputTokenBase64").value = "Error: " + xhr.statusText;
//     }
//   };

//   xhr.onerror = function () {
//     console.log("outputTokenBase64").value = "Error making request";
//   };

//   xhr.send(data);
// }

// function convertBase64() {
//   const url = "/public/api/tools/base64";
//   const data = document.getElementById("inputTokenBase64").value;

//   const xhr = new XMLHttpRequest();
//   xhr.open("POST", url); // Set the URL from the form's action attribute
//   xhr.setRequestHeader("Content-Type", "text/plain"); // Set content type for string

//   xhr.onload = function () {
//     if (xhr.status === 200) {
//       document.getElementById("outputTokenBase64").value = xhr.responseText;
//     } else {
//       document.getElementById("outputTokenBase64").value =
//         "Error: " + xhr.statusText;
//     }
//   };

//   xhr.onerror = function () {
//     document.getElementById("outputTokenBase64").value = "Error making request";
//   };

//   xhr.send(data);
// }

// function convertJwt() {
//   const url = "/public/api/tools/jwt";
//   const data = document.getElementById("inputToken").value;

//   const xhr = new XMLHttpRequest();
//   xhr.open("POST", url); // Set the URL from the form's action attribute
//   xhr.setRequestHeader("Content-Type", "text/plain"); // Set content type for string

//   xhr.onload = function () {
//     if (xhr.status === 200) {
//       document.getElementById("outputToken").textContent = xhr.responseText;
//     } else {
//       document.getElementById("outputToken").textContent =
//         "Error: " + xhr.statusText;
//     }
//   };

//   xhr.onerror = function () {
//     document.getElementById("outputToken").textContent = "Error making request";
//   };

//   xhr.send(data);
//   // $.ajax({
//   //   url: url,
//   //   method: "POST",
//   //   contentType: "text/plain",
//   //   data: data,
//   //   success: function(response) {
//   //     // Handle successful response and update the response div
//   //     $("#outputToken").text(JSON.stringify(response));
//   //   },
//   //   error: function(error) {
//   //     // Handle error and display an appropriate message
//   //     $("#outputToken").text("Error: " + error.responseText);
//   //   }
//   // });
// }
