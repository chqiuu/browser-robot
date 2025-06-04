// content.js

// 监听来自后台服务工作者的消息
chrome.runtime.onMessage.addListener(async (message, sender, sendResponse) => {
    if (message.action === "search_baidu") {
        console.log("Content script received search command:", message.query);
        try {
            await performBaiduSearch(message.query);

            // 等待搜索结果加载 (实际中可能需要更复杂的等待机制，例如 MutationObserver)
            await new Promise(resolve => setTimeout(resolve, 3000));

            const results = extractSearchResults();
            console.log("Extracted search results:", results);
            sendResponse({ status: "success", results: results });

        } catch (error) {
            console.error("Error in content script during search or extraction:", error);
            sendResponse({ status: "error", message: error.message });
        }
        return true; // 表示异步响应
    }
});

// 在百度页面执行搜索操作
async function performBaiduSearch(query) {
    // 查找百度搜索输入框 (可能ID或class会变动，需要检查百度当前页面结构)
    let searchInput = document.getElementById('kw') || document.querySelector('.s_ipt');
    let searchButton = document.getElementById('su') || document.querySelector('.s_btn');

    if (searchInput && searchButton) {
        searchInput.value = query;
        searchButton.click(); // 点击搜索按钮
        console.log(`Performed search for: ${query}`);
    } else {
        throw new Error("Baidu search input or button not found.");
    }
}

// 提取搜索结果
function extractSearchResults() {
    const results = [];
    // 查找搜索结果列表的父容器，这里使用一个常见的ID或类名，实际需检查百度页面
    const resultListContainer = document.getElementById('content_left') || document.querySelector('#container #content_left');

    if (resultListContainer) {
        // 查找每个搜索结果项，这里使用常见的类名，实际需检查百度页面
        // Baidu 的搜索结果项 class 通常是 `c-container` 或 `result`
        const resultItems = resultListContainer.querySelectorAll('.c-container');

        resultItems.forEach((item, index) => {
            if (index >= 5) return; // 只获取前5条结果作为示例

            const titleElement = item.querySelector('.t > a') || item.querySelector('h3 > a');
            const linkElement = titleElement; // 链接通常在标题的a标签上
            const snippetElement = item.querySelector('.c-abstract') || item.querySelector('.c-span-content');

            const title = titleElement ? titleElement.innerText.trim() : 'N/A';
            const link = linkElement ? linkElement.href : 'N/A';
            const snippet = snippetElement ? snippetElement.innerText.trim() : 'N/A';

            results.push({ title, link, snippet });
        });
    } else {
        console.warn("Search results container not found. Check Baidu's DOM structure.");
    }
    return results;
}