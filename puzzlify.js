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

async function puzzlify(rows, cols){
    console.log(rows, cols);

    //post 
    const imageView = document.getElementById("image-view");
    var fd = new FormData();
    fd.append('image', document.getElementById("input-file").files[0]);

    const qstr = new URLSearchParams({rows, cols}).toString(); //makes a query string 
    const url = new URL(`http://localhost:7070/puzzlify?${qstr}`);

    try {
        const response = await fetch(url,{
            method: 'post',
            body: fd
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
        const puzzle = await response; //dont need to make it json

        imageView.style.backgroundImage = `url(${puzzle})`;
        imageView.style.backgroundRepeat = `no-repeat`;
        imageView.style.backgroundPosition = `center`;
        imageView.textContent = "";

        console.log(data);
    } catch (error) {
        console.error("Fetch failed:", error);
    }
    
    //make a request to localhost:7070/puzzlify
    //pass in uploaded image as file and rows and cols as ints
    //await the response then preview the output image
    //the response is a url
}



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

