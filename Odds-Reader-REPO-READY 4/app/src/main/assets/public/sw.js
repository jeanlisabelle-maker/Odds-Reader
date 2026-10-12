
const CACHE="odds-reader-v12";
const ASSETS=["./","./Odds-Reader-TEST-12.html","./manifest.json"];
self.addEventListener("install",e=>{
  e.waitUntil(caches.open(CACHE).then(c=>c.addAll(ASSETS)).then(()=>self.skipWaiting()));
});
self.addEventListener("activate",e=>{
  e.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(k=>k!==CACHE).map(k=>caches.delete(k)))).then(()=>self.clients.claim()));
});
self.addEventListener("fetch",e=>{
  // Share target GET ?text=... should go to network
  if(e.request.url.includes("?text=") || e.request.url.includes("?share=") || e.request.url.includes("?url=")){
    e.respondWith(fetch(e.request));
    return;
  }
  e.respondWith(caches.match(e.request).then(r=>r||fetch(e.request).then(res=>{
    // cache HTML updates
    if(e.request.method==="GET" && res.ok && e.request.url.endsWith(".html")){
      const clone=res.clone();
      caches.open(CACHE).then(c=>c.put(e.request, clone));
    }
    return res;
  })));
});
