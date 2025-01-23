// Image processing functions implemented in imageService.js

// Adjust brightness
function adjustBrightness(image, value) {
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");

    canvas.width = image.width;
    canvas.height = image.height;

    ctx.drawImage(image, 0, 0);

    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
    const data = imageData.data;

    for (let i = 0; i < data.length; i += 4) {
        data[i] = data[i] + value; // Red
        data[i + 1] = data[i + 1] + value; // Green
        data[i + 2] = data[i + 2] + value; // Blue
    }

    ctx.putImageData(imageData, 0, 0);

    return new Promise((resolve) => {
        canvas.toBlob((blob) => {
            resolve(new File([blob], image.name, { type: image.type }));
        });
    });
}

// Adjust contrast
function adjustContrast(image, value) {
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");

    canvas.width = image.width;
    canvas.height = image.height;

    ctx.drawImage(image, 0, 0);

    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
    const data = imageData.data;

    const factor = (259 * (value + 255)) / (255 * (259 - value));

    for (let i = 0; i < data.length; i += 4) {
        data[i] = factor * (data[i] - 128) + 128; // Red
        data[i + 1] = factor * (data[i + 1] - 128) + 128; // Green
        data[i + 2] = factor * (data[i + 2] - 128) + 128; // Blue
    }

    ctx.putImageData(imageData, 0, 0);

    return new Promise((resolve) => {
        canvas.toBlob((blob) => {
            resolve(new File([blob], image.name, { type: image.type }));
        });
    });
}

// Apply grayscale filter
function applyGrayscale(image) {
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");

    canvas.width = image.width;
    canvas.height = image.height;

    ctx.drawImage(image, 0, 0);

    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
    const data = imageData.data;

    for (let i = 0; i < data.length; i += 4) {
        const avg = (data[i] + data[i + 1] + data[i + 2]) / 3;
        data[i] = avg; // Red
        data[i + 1] = avg; // Green
        data[i + 2] = avg; // Blue
    }

    ctx.putImageData(imageData, 0, 0);

    return new Promise((resolve) => {
        canvas.toBlob((blob) => {
            resolve(new File([blob], image.name, { type: image.type }));
        });
    });
}

// Apply binarization
function applyBinarization(image, threshold) {
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");

    canvas.width = image.width;
    canvas.height = image.height;

    ctx.drawImage(image, 0, 0);

    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
    const data = imageData.data;

    for (let i = 0; i < data.length; i += 4) {
        const avg = (data[i] + data[i + 1] + data[i + 2]) / 3;
        const binarized = avg >= threshold ? 255 : 0;
        data[i] = binarized; // Red
        data[i + 1] = binarized; // Green
        data[i + 2] = binarized; // Blue
    }

    ctx.putImageData(imageData, 0, 0);

    return new Promise((resolve) => {
        canvas.toBlob((blob) => {
            resolve(new File([blob], image.name, { type: image.type }));
        });
    });
}

// Flip image horizontally
function flipImageHorizontally(image) {
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");

    canvas.width = image.width;
    canvas.height = image.height;

    ctx.translate(canvas.width, 0);
    ctx.scale(-1, 1);
    ctx.drawImage(image, 0, 0);

    return new Promise((resolve) => {
        canvas.toBlob((blob) => {
            resolve(new File([blob], image.name, { type: image.type }));
        });
    });
}

// Flip image vertically
function flipImageVertically(image) {
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");

    canvas.width = image.width;
    canvas.height = image.height;

    ctx.translate(0, canvas.height);
    ctx.scale(1, -1);
    ctx.drawImage(image, 0, 0);

    return new Promise((resolve) => {
        canvas.toBlob((blob) => {
            resolve(new File([blob], image.name, { type: image.type }));
        });
    });
}
function resizeImage(image, width, height) {
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");

    canvas.width = width;
    canvas.height = height;

    ctx.drawImage(image, 0, 0, width, height);

    return new Promise((resolve) => {
        canvas.toBlob((blob) => {
            resolve(new File([blob], image.name, { type: image.type }));
        });
    });
}
function applyBlur(image, kernelSize) {
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");

    canvas.width = image.width;
    canvas.height = image.height;

    ctx.filter = `blur(${kernelSize}px)`;
    ctx.drawImage(image, 0, 0);

    return new Promise((resolve) => {
        canvas.toBlob((blob) => {
            resolve(new File([blob], image.name, { type: image.type }));
        });
    });
}
function applySharpen(image) {
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");

    canvas.width = image.width;
    canvas.height = image.height;

    ctx.drawImage(image, 0, 0);

    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
    const data = imageData.data;

    const kernel = [
        0, -1, 0,
        -1, 5, -1,
        0, -1, 0
    ];

    const result = applyKernel(data, imageData.width, imageData.height, kernel, 1);
    ctx.putImageData(new ImageData(result, canvas.width, canvas.height), 0, 0);

    return new Promise((resolve) => {
        canvas.toBlob((blob) => {
            resolve(new File([blob], image.name, { type: image.type }));
        });
    });
}

function applyKernel(data, width, height, kernel, divisor) {
    const output = new Uint8ClampedArray(data.length);
    const half = Math.floor(Math.sqrt(kernel.length) / 2);

    for (let y = 0; y < height; y++) {
        for (let x = 0; x < width; x++) {
            let r = 0, g = 0, b = 0;

            for (let ky = -half; ky <= half; ky++) {
                for (let kx = -half; kx <= half; kx++) {
                    const i = ((y + ky) * width + (x + kx)) * 4;
                    const weight = kernel[(ky + half) * (half * 2 + 1) + (kx + half)];

                    if (i >= 0 && i < data.length) {
                        r += data[i] * weight;
                        g += data[i + 1] * weight;
                        b += data[i + 2] * weight;
                    }
                }
            }

            const idx = (y * width + x) * 4;
            output[idx] = Math.min(Math.max(r / divisor, 0), 255);
            output[idx + 1] = Math.min(Math.max(g / divisor, 0), 255);
            output[idx + 2] = Math.min(Math.max(b / divisor, 0), 255);
            output[idx + 3] = data[idx + 3];
        }
    }

    return output;
}
function applyEdgeDetection(image, type = "sobel") {
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");

    canvas.width = image.width;
    canvas.height = image.height;

    ctx.drawImage(image, 0, 0);

    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
    const data = imageData.data;

    const kernelX = type === "sobel" ? [-1, 0, 1, -2, 0, 2, -1, 0, 1] : [-1, -1, -1, 0, 0, 0, 1, 1, 1];
    const kernelY = type === "sobel" ? [-1, -2, -1, 0, 0, 0, 1, 2, 1] : [-1, -1, -1, 0, 0, 0, 1, 1, 1];

    const result = applyKernel(data, canvas.width, canvas.height, kernelX, 1);
    ctx.putImageData(new ImageData(result, canvas.width, canvas.height), 0, 0);

    return new Promise((resolve) => {
        canvas.toBlob((blob) => {
            resolve(new File([blob], image.name, { type: image.type }));
        });
    });
}
function removeNoise(image) {
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");

    canvas.width = image.width;
    canvas.height = image.height;

    ctx.filter = "blur(2px)";
    ctx.drawImage(image, 0, 0);

    return new Promise((resolve) => {
        canvas.toBlob((blob) => {
            resolve(new File([blob], image.name, { type: image.type }));
        });
    });
}

// Export functions for usage in Vue.js
export {
    adjustBrightness,
    adjustContrast,
    applyGrayscale,
    applyBinarization,
    flipImageHorizontally,
    flipImageVertically,
    resizeImage,
    applyBlur,
    applySharpen,
    applyEdgeDetection,
    removeNoise
};
