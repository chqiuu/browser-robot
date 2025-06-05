// content_script.js

console.log("content_script.js 已加载到页面。");

// 辅助函数：等待某个DOM元素出现
function waitForElement(selector, timeout = 10000) {
    return new Promise((resolve, reject) => {
        const element = document.querySelector(selector);
        if (element) {
            return resolve(element);
        }

        const observer = new MutationObserver((mutations, obs) => {
            const foundElement = document.querySelector(selector);
            if (foundElement) {
                obs.disconnect();
                resolve(foundElement);
            }
        });

        observer.observe(document.body, { childList: true, subtree: true });

        setTimeout(() => {
            observer.disconnect();
            reject(new Error(`Element "${selector}" not found within timeout.`));
        }, timeout);
    });
}


// 执行Baidu搜索的函数
async function performBaiduSearch(query) {
    console.log("尝试搜索:", query);

    try {
        // 等待搜索输入框出现
        const searchInput = await waitForElement('#kw, #wd'); // Baidu搜索框的常见ID

        // 等待搜索按钮出现
        const searchButton = await waitForElement('#su, #s_btn_wr, #sb_form_go'); // Baidu搜索按钮的常见ID/类

        if (!searchInput || !searchButton) {
            const errorMessage = `Baidu搜索元素未找到。searchInput: ${!!searchInput}, searchButton: ${!!searchButton}`;
            console.error(errorMessage);
            // 将错误发送回background脚本
            chrome.runtime.sendMessage({ type: "error", message: errorMessage });
            return { status: "error", message: errorMessage };
        }

        searchInput.value = query;
        searchButton.click();
        console.log("搜索关键词已输入并点击搜索按钮。");

        // 等待搜索结果加载 (例如，等待左侧结果容器出现)
        // Baidu搜索结果通常在 #content_left 容器内
        await waitForElement('#content_left', 15000); // 15秒等待搜索结果

        console.log("搜索结果区域已加载。开始抓取结果...");
        const results = scrapeSearchResults();
        return { status: "success", results: results };

    } catch (e) {
        console.error("执行搜索时发生错误:", e.message);
        // 将错误发送回background脚本
        chrome.runtime.sendMessage({ type: "error", message: `Baidu搜索执行失败: ${e.message}` });
        return { status: "error", message: `Search failed: ${e.message}` };
    }
}

// 抓取搜索结果的函数
function scrapeSearchResults() {
    const results = [];
    // Baidu搜索结果项常见的CSS选择器：.result 或 .c-container
    const resultElements = document.querySelectorAll('.result, .c-container');

    resultElements.forEach(element => {
        const titleElement = element.querySelector('h3 a'); // 标题链接
        const urlElement = element.querySelector('.c-showurl'); // 显示的URL，通常是原始URL
        const snippetElement = element.querySelector('.c-abstract, .op_desc, .content-summary'); // 摘要/描述

        let title = titleElement ? titleElement.innerText.trim() : '无标题';
        let url = titleElement ? titleElement.href : '无URL'; // 优先使用标题链接的href
        let snippet = snippetElement ? snippetElement.innerText.trim() : '无摘要';

        // 尝试获取显示URL，有时会更准确
        if (urlElement && urlElement.innerText.trim() !== '') {
            // 注意：Baidu的实际跳转URL是经过重定向的。
            // 如果需要原始URL，需要更复杂的解析，例如从标题链接的data属性中提取，或者进行二次HTTP请求。
            // 这里我们先取标题链接的href
        }

        // 清理URL中的Baidu重定向前缀
        if (url.startsWith('http://www.baidu.com/link?url=')) {
            // Baidu的链接通常是重定向链接，直接使用通常会跳到Baidu的跳转页面。
            // 要获取真实的最终URL，需要更复杂的逻辑，例如在后端使用无头浏览器访问此URL，
            // 或者通过解析Baidu页面中隐藏的真实URL（如果存在）。
            // For simplicity, we'll keep the redirect URL for now.
        }

        results.push({ title, url, snippet });
    });
    console.log("已抓取结果:", results);
    return results;
}

// 监听来自后台脚本的消息
chrome.runtime.onMessage.addListener(async (message, sender, sendResponse) => {
    if (message.command === "search") {
        console.log("内容脚本收到搜索命令:", message.query);
        const result = await performBaiduSearch(message.query);

        if (result.status === "success" || result.status === "timeout") {
            // 将搜索结果发送回后台脚本
            chrome.runtime.sendMessage({ type: "search_results", results: result.results });
        } else {
            console.error("搜索失败或超时:", result.message);
            chrome.runtime.sendMessage({ type: "error", message: `搜索失败: ${result.message}` });
        }
        sendResponse({ status: "processed", details: result.message }); // 确认消息已处理
        return true; // 返回 true 以指示我们将异步发送响应
    }
});