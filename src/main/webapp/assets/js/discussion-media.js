"use strict";
document.addEventListener("DOMContentLoaded",()=>{
 const form=document.querySelector("[data-media-composer]"); if(!form)return;
 const input=form.querySelector("[data-attachment-input]"),preview=form.querySelector("[data-media-preview]"),record=form.querySelector("[data-voice-record]");
 let recorder,chunks=[],stream;
 const show=(text,withCancel=true)=>{preview.hidden=false;preview.replaceChildren(Object.assign(document.createElement("span"),{textContent:text}));if(withCancel){const cancel=Object.assign(document.createElement("button"),{type:"button",textContent:"Cancel"});cancel.addEventListener("click",()=>{input.value="";preview.hidden=true;preview.replaceChildren()});preview.append(cancel)}};
 input.addEventListener("change",()=>{const file=input.files?.[0];if(file)show(`${file.name} · ${Math.max(1,Math.round(file.size/1024))} KB`)});
 record.addEventListener("click",async()=>{
  if(!window.MediaRecorder){show("Voice recording is not supported in this browser.",false);return}
  if(recorder?.state==="recording"){recorder.stop();record.textContent="Mic";return}
  try{stream=await navigator.mediaDevices.getUserMedia({audio:true});chunks=[];recorder=new MediaRecorder(stream);recorder.ondataavailable=e=>{if(e.data.size)chunks.push(e.data)};recorder.onstop=()=>{stream.getTracks().forEach(t=>t.stop());const type=recorder.mimeType||"audio/webm",blob=new Blob(chunks,{type}),file=new File([blob],`voice-${Date.now()}.webm`,{type});const transfer=new DataTransfer();transfer.items.add(file);input.files=transfer.files;preview.hidden=false;preview.replaceChildren();const audio=document.createElement("audio");audio.controls=true;audio.src=URL.createObjectURL(blob);const cancel=Object.assign(document.createElement("button"),{type:"button",textContent:"Cancel"});cancel.addEventListener("click",()=>{URL.revokeObjectURL(audio.src);input.value="";preview.hidden=true;preview.replaceChildren()});preview.append(audio,cancel)};recorder.start();record.textContent="Stop";show("Recording… click Stop to review.",false)}catch(e){show("Microphone permission was denied. You can still send text or attach a file.",false)}
 });
});
