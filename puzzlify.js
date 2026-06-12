// import "@fontsource-variable/material-symbols-rounded";  //logo subtitle font
// import "@fontsource-variable/material-symbols-rounded/wght.css"; 
// import "@fontsource-variable/material-symbols-rounded/wght-italic.css"; 
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

class PuzzlePiece extends CustomElement {
    constructor() {
        super('puzzle-piece-template');
    }
}
customElements.define('puzzle-piece', PuzzlePiece);

window.onload = () => {
    const dropArea = document.getElementById("drop-area");
    const inputFile = document.getElementById("input-file");
    const imageView = document.getElementById("image-view");
    
    inputFile.addEventListener("change", uploadImage);
    
    function uploadImage(){
        let imageLink = URL.createObjectURL( inputFile.files[0]);
        imageView.style.backgroundImage = `url(${imageLink})`;
        imageView.style.backgroundRepeat = `no-repeat`;
        imageView.style.backgroundPosition = `center`;
        imageView.textContent = "";
    }
    dropArea.addEventListener("dragover", function(e){
        e.preventDefault();
       
    });
     dropArea.addEventListener("drop", function(e){
        e.preventDefault();
        inputFile.files = e.dataTransfer.files;
        uploadImage();
    });
}