class CustomElement extends HTMLElement {
    constructor(id) {
        super();
        console.log(`[CustomElement] >> initializing with template id: ${id}`);
        const template = document.getElementById(id);
        
        // assumption: template exists
        if (template) {
            // instead of using shadow DOM which makes Alpine.js reactivity complex, 
            // append directly to the light DOM for simpler interop.
            this.appendChild(template.content.cloneNode(true)); 
        } else {
            console.error("[CustomElement] >> template not found!");
        }
    }
}