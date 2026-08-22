(function(){
 "use strict";
 if(window.StudentHubLanguage)return;
 const storageKey="studenthub-language";
 const messages={
  en:{home:"Home",features:"Features",how:"How It Works",about:"About",announcements:"Announcements",notifications:"Notifications",discussions:"Discussions / Chat",profile:"Profile",signin:"Sign In",register:"Register",logout:"Logout"},
  my:{home:"ပင်မ",features:"လုပ်ဆောင်ချက်များ",how:"အသုံးပြုပုံ",about:"အကြောင်း",announcements:"ကြေညာချက်များ",notifications:"အသိပေးချက်များ",discussions:"ဆွေးနွေးခန်း / စကားပြောခန်း",profile:"ကိုယ်ရေးအချက်အလက်",signin:"အကောင့်ဝင်ရန်",register:"အကောင့်ဖွင့်ရန်",logout:"အကောင့်ထွက်ရန်"}
 };
 const known={
  "Verify your email":"သင့်အီးမေးလ်ကို အတည်ပြုပါ","Verify email":"အီးမေးလ် အတည်ပြုရန်","Resend code":"ကုဒ်ပြန်ပို့ရန်",
  "Forgot password":"စကားဝှက် မေ့နေပါသလား","Reset password":"စကားဝှက် ပြန်လည်သတ်မှတ်ရန်","Create account":"အကောင့်ဖွင့်ရန်",
  "Latest updates":"နောက်ဆုံးအပ်ဒိတ်များ","Upcoming Deadlines":"လာမည့် နောက်ဆုံးရက်များ","Create Post":"ပို့စ်ဖန်တီးရန်",
  "Edit profile":"ကိုယ်ရေးအချက်အလက် ပြင်ရန်","Save changes":"ပြင်ဆင်ချက်များ သိမ်းရန်","Mark as read":"ဖတ်ပြီးအဖြစ် မှတ်ရန်",
  "View":"ကြည့်ရန်","Send":"ပို့ရန်","Menu":"မီနူး","All":"အားလုံး","Unread":"မဖတ်ရသေး"
 };
 function translateKnownUi(lang){document.querySelectorAll("h1,h2,label,button,a.button,.eyebrow").forEach(el=>{if(el.closest(".post-card,.message-bubble,.comment-content,.notification-content"))return;if(!el.dataset.i18nOriginal)el.dataset.i18nOriginal=el.textContent.trim();const original=el.dataset.i18nOriginal;if(lang==="my"&&known[original])el.textContent=known[original];else if(lang==="en"&&known[original])el.textContent=original})}
 function apply(language){const lang=language==="my"?"my":"en";localStorage.setItem(storageKey,lang);document.documentElement.lang=lang;document.body?.classList.toggle("myanmar-language",lang==="my");document.querySelectorAll("[data-i18n]").forEach(el=>{const value=messages[lang][el.dataset.i18n];if(value)el.textContent=value});document.querySelectorAll("[data-language]").forEach(button=>button.setAttribute("aria-pressed",String(button.dataset.language===lang)));translateKnownUi(lang)}
 document.addEventListener("click",event=>{const button=event.target.closest("[data-language]");if(button)apply(button.dataset.language)});
 document.addEventListener("DOMContentLoaded",()=>apply(localStorage.getItem(storageKey)||"en"));
 window.StudentHubLanguage={apply};
})();
