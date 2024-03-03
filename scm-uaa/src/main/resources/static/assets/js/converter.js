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

function convertTime() {
  const url = "/public/api/tools/time";
  const data = document.getElementById("inputLongTime").value;

  const xhr = new XMLHttpRequest();
  xhr.open("POST", url); // Set the URL from the form's action attribute
  xhr.setRequestHeader("Content-Type", "text/plain"); // Set content type for string

  xhr.onload = function() {
    if (xhr.status === 200) {
      document.getElementById("inputLongFormattedTime").value = xhr.responseText;
    } else {
      document.getElementById("inputLongFormattedTime").value = "Error: " + xhr.statusText;
    }
  };

  xhr.onerror = function() {
    document.getElementById("inputLongFormattedTime").value = "Error making request";
  };

  xhr.send(data);
}

function convertLogin() {
  const url = "/oauth2/token";
  const grant_type = "first_password"; // || second_password
  const username = "rezajms";
  const password = "test";
  const scope = "";//"session";
  const client_id = "ib";
  const client_version = "";
  const client_signature = "";
  const register_code = "";
  const access_parameter = "asdsad";
  const claim_code = "456";
  if (isEmptyString(username)) {
    alert('invalid username');
    return;
  }
  if (isEmptyString(password)) {
    alert('invalid password');
    return;
  }
  if (isEmptyString(client_id)) {
    alert('invalid client_id');
    return;
  }
  if (isEmptyString(access_parameter)) {
    alert('invalid access_parameter');
    return;
  }
  const params = new URLSearchParams();
  params.append('grant_type', grant_type);
  params.append('username', username);
  params.append('password', password);
  params.append('client_id', client_id);
  params.append('access_parameter', access_parameter);
  if (isNotEmptyStr(scope)) {
    params.append('scope', scope);
  }
  if (isNotEmptyStr(client_version)) {
    params.append('client_version', client_version);
  }
  if (isNotEmptyStr(client_signature)) {
    params.append('client_signature', client_signature);
  }
  if (isNotEmptyStr(register_code)) {
    params.append('register_code', register_code);
  }
  if (isNotEmptyStr(claim_code)) {
    params.append('claim_code', claim_code);
  }

  const data = params.toString();

  const xhr = new XMLHttpRequest();
  xhr.open("POST", url); // Set the URL from the form's action attribute
  xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded"); // Set content type for string

  xhr.onload = function() {
    if (xhr.status === 200) {
      console.log("outputTokenBase64").value = xhr.responseText;
    } else {
      console.log("outputTokenBase64").value = "Error: " + xhr.statusText;
    }
  };

  xhr.onerror = function() {
    console.log("outputTokenBase64").value = "Error making request";
  };

  xhr.send(data);
}

function convertBase64() {
  const url = "/public/api/tools/base64";
  const data = document.getElementById("inputTokenBase64").value;

  const xhr = new XMLHttpRequest();
  xhr.open("POST", url); // Set the URL from the form's action attribute
  xhr.setRequestHeader("Content-Type", "text/plain"); // Set content type for string

  xhr.onload = function() {
    if (xhr.status === 200) {
      document.getElementById("outputTokenBase64").value = xhr.responseText;
    } else {
      document.getElementById("outputTokenBase64").value = "Error: " + xhr.statusText;
    }
  };

  xhr.onerror = function() {
    document.getElementById("outputTokenBase64").value = "Error making request";
  };

  xhr.send(data);
}

function convertJwt() {
  const url = "/public/api/tools/jwt";
  const data = document.getElementById("inputToken").value;

  const xhr = new XMLHttpRequest();
  xhr.open("POST", url); // Set the URL from the form's action attribute
  xhr.setRequestHeader("Content-Type", "text/plain"); // Set content type for string

  xhr.onload = function() {
    if (xhr.status === 200) {
      document.getElementById("outputToken").textContent = xhr.responseText;
    } else {
      document.getElementById("outputToken").textContent = "Error: " + xhr.statusText;
    }
  };

  xhr.onerror = function() {
    document.getElementById("outputToken").textContent = "Error making request";
  };

  xhr.send(data);
  // $.ajax({
  //   url: url,
  //   method: "POST",
  //   contentType: "text/plain",
  //   data: data,
  //   success: function(response) {
  //     // Handle successful response and update the response div
  //     $("#outputToken").text(JSON.stringify(response));
  //   },
  //   error: function(error) {
  //     // Handle error and display an appropriate message
  //     $("#outputToken").text("Error: " + error.responseText);
  //   }
  // });
}

function isEmptyString(str) {
  // Handle null or undefined values
  if (str === null || str === undefined) {
    return true;
  }

  // Trim whitespace and check for empty string
  return str.trim() === "";
}

function isNotEmptyStr(str) {
  // Combine conditions using logical NOT operator (!)
  return !(str === null || str === undefined || str.trim() === "");
}