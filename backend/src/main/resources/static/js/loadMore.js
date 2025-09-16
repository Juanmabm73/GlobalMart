let page = 0;

async function loadMoreAll() {
    try {
        page++;

        // Show loading state
        const loadMoreBtn = document.getElementById("loadMoreBtn");
        if (loadMoreBtn) {
            loadMoreBtn.disabled = true;
            loadMoreBtn.textContent = "Loading...";
        }

        // Fetch more products
        const response = await fetch(`/moreProdsAll?page=${page}`);

        if (!response.ok) {
            throw new Error('Network response was not ok');
        }

        const data = await response.text();
        const productsContainer = document.getElementById("productsContainer");

        if (productsContainer) {
            // Append the new products to the container
            productsContainer.innerHTML += data;
        }

        // Reset button state
        if (loadMoreBtn) {
            loadMoreBtn.disabled = false;
            loadMoreBtn.textContent = "Load More";
        }

        // Check if there are more products to load
        if (data.includes("<!--HasMoreElements-->")) {
            if (loadMoreBtn) {
                loadMoreBtn.style.display = "block";
            }
        } else {
            if (loadMoreBtn) {
                loadMoreBtn.style.display = "none";
            }
        }
    } catch (error) {
        console.error('Error loading more products:', error);

        // Reset button state on error
        const loadMoreBtn = document.getElementById("loadMoreBtn");
        if (loadMoreBtn) {
            loadMoreBtn.disabled = false;
            loadMoreBtn.textContent = "Load More";
        }
    }
}