let pageCompany = 0;


async function loadMoreCompany(companyName) {
    pageCompany++;
    document.getElementById("loadMoreCompany").style.display = "block";
    let response = await fetch(`/moreProdsCompany?page=${pageCompany}&company=${companyName}`);
    let data = await response.text();
    document.getElementById("productsContainer").innerHTML += data;

    if (data.includes("<!--HasMoreElements-->")){
        document.getElementById("loadMoreCompany").style.display = "block";
    }else{
        document.getElementById("loadMoreCompany").style.display = "none";
    }

}