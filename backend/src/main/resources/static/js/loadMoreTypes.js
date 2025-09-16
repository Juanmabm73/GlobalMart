let pageType = 0;


async function loadMoreTypes(type) {
    pageType++;
    document.getElementById("loadMoreTypes").style.display = "block";

    let response = await fetch(`/moreProdsTypes?page=${pageType}&type=${type}`);
    let data = await response.text();
    document.getElementById("productsContainer").innerHTML += data;

    if (data.includes("<!--HasMoreElements-->")) {
        document.getElementById("loadMoreTypes").style.display = "block";
    } else {
        document.getElementById("loadMoreTypes").style.display = "none";
    }
}