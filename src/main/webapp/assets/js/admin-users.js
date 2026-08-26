document.addEventListener("DOMContentLoaded",()=>{
  const tabs=[...document.querySelectorAll('[role="tab"][data-role-tab]')];
  const panels=[...document.querySelectorAll('[role="tabpanel"][data-role-section]')];
  if(!tabs.length)return;
  const activate=tab=>{tabs.forEach(item=>{const active=item===tab;item.setAttribute("aria-selected",String(active));item.tabIndex=active?0:-1;});panels.forEach(panel=>panel.hidden=panel.id!==tab.getAttribute("aria-controls"));};
  tabs.forEach((tab,index)=>{tab.addEventListener("click",()=>activate(tab));tab.addEventListener("keydown",event=>{let next;if(event.key==="ArrowRight")next=(index+1)%tabs.length;if(event.key==="ArrowLeft")next=(index+tabs.length-1)%tabs.length;if(event.key==="Home")next=0;if(event.key==="End")next=tabs.length-1;if(next!==undefined){event.preventDefault();activate(tabs[next]);tabs[next].focus();}});});
});
