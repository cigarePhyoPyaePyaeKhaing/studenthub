document.addEventListener("DOMContentLoaded",()=>{
  const modal=document.querySelector("[data-delete-account-modal]");
  const open=document.querySelector("[data-open-delete-account]");
  const close=[...document.querySelectorAll("[data-close-delete-account]")];
  const form=document.querySelector("[data-delete-account-form]");
  if(!modal||!open||!form)return;
  const password=form.querySelector('[name="currentPassword"]');
  const submit=form.querySelector('[type="submit"]');
  const error=form.querySelector("[data-delete-account-error]");
  const hide=()=>{modal.hidden=true;password.value="";error.textContent="";open.focus();};
  open.addEventListener("click",()=>{modal.hidden=false;password.focus();});
  close.forEach(button=>button.addEventListener("click",hide));
  modal.addEventListener("click",event=>{if(event.target===modal)hide();});
  document.addEventListener("keydown",event=>{if(event.key==="Escape"&&!modal.hidden)hide();});
  form.addEventListener("submit",async event=>{
    event.preventDefault(); if(submit.disabled)return;
    submit.disabled=true; submit.textContent="Deleting..."; error.textContent="";
    try{
      const response=await fetch(form.action,{method:"POST",body:new FormData(form),credentials:"same-origin",headers:{Accept:"application/json"}});
      const payload=await response.json();
      if(response.ok&&payload.success){window.location.assign(payload.redirectUrl);return;}
      error.textContent=payload.message||"Your account could not be deleted right now.";
    }catch(_){error.textContent="Your account could not be deleted right now.";}
    submit.disabled=false; submit.textContent="Permanently Delete Account";
  });
});
