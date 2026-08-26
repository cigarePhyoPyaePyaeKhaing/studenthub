document.addEventListener("DOMContentLoaded",()=>{
  const modal=document.querySelector("[data-delete-account-modal]");
  const open=document.querySelector("[data-open-delete-account]");
  const close=[...document.querySelectorAll("[data-close-delete-account]")];
  const form=document.querySelector("[data-delete-account-form]");
  if(!modal||!open||!form)return;
  const password=form.querySelector('[name="currentPassword"]');
  const submit=form.querySelector('[type="submit"]');
  const error=form.querySelector("[data-delete-account-error]");
  const csrf=form.querySelector('[name="csrfToken"]');
  const messages={
    ACCOUNT_DELETE_PASSWORD_INVALID:"Incorrect password.",
    ACCOUNT_DELETE_CSRF_INVALID:"Your security session expired. Refresh the page and try again.",
    ACCOUNT_DELETE_UNAUTHENTICATED:"Your session has expired. Please sign in again.",
    ACCOUNT_DELETE_LAST_ADMIN:"StudentHub must have at least one administrator.",
    ACCOUNT_DELETE_CONFLICT:"Your account could not be deleted. Please refresh and try again.",
    ACCOUNT_DELETE_DB_ERROR:"Your account could not be deleted. Please try again.",
    ACCOUNT_DELETE_SERVER_ERROR:"Your account could not be deleted. Please try again.",
    ACCOUNT_DELETE_NETWORK_FAILED:"Could not reach StudentHub. Check your connection and try again.",
    ACCOUNT_DELETE_RESPONSE_INVALID:"StudentHub returned an invalid response. Please try again.",
    ACCOUNT_DELETE_CLIENT_ERROR:"Your account could not be deleted. Please try again."
  };
  const hide=()=>{modal.hidden=true;password.value="";error.textContent="";open.focus();};
  open.addEventListener("click",()=>{modal.hidden=false;password.focus();});
  close.forEach(button=>button.addEventListener("click",hide));
  modal.addEventListener("click",event=>{if(event.target===modal)hide();});
  document.addEventListener("keydown",event=>{if(event.key==="Escape"&&!modal.hidden)hide();});
  form.addEventListener("submit",async event=>{
    event.preventDefault(); if(submit.disabled)return;
    submit.disabled=true; submit.textContent="Deleting..."; error.textContent="";
    try{
      if(!password||!csrf)throw Object.assign(new Error("Delete account form is incomplete"),{accountDeleteCode:"ACCOUNT_DELETE_CLIENT_ERROR"});
      const body=new URLSearchParams(); body.set("currentPassword",password.value); body.set("csrfToken",csrf.value);
      console.info("ACCOUNT_DELETE_BEFORE_FETCH");
      const response=await fetch(form.action,{method:"POST",body,credentials:"same-origin",headers:{Accept:"application/json","Content-Type":"application/x-www-form-urlencoded;charset=UTF-8"}});
      console.info("ACCOUNT_DELETE_AFTER_FETCH",{status:response.status});
      console.info("ACCOUNT_DELETE_BEFORE_PARSE");
      let payload;try{payload=JSON.parse(await response.text());}catch(parseError){throw Object.assign(parseError,{accountDeleteCode:"ACCOUNT_DELETE_RESPONSE_INVALID"});}
      console.info("ACCOUNT_DELETE_AFTER_PARSE",{code:payload&&payload.code});
      if(response.ok&&payload.success){window.location.assign(payload.redirectUrl);return;}
      error.textContent=messages[payload.code]||"Your account could not be deleted. Please try again.";
    }catch(exception){
      const code=exception.accountDeleteCode||(exception instanceof TypeError?"ACCOUNT_DELETE_NETWORK_FAILED":"ACCOUNT_DELETE_CLIENT_ERROR");
      console.error(code,{name:exception.name,message:exception.message,stack:exception.stack});
      error.textContent=messages[code];
    }
    submit.disabled=false; submit.textContent="Permanently Delete Account";
  });
});
