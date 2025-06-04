// background.js

const BAIDU_URL = "https://www.baidu.com/";
const SEARCH_QUERY = "哪里有好吃的";
const JAVA_BACKEND_URL = "http://localhost:8080/api/search-results"; // 替换为你的Java后端接收结果的URL

/**
 * 模拟Java后端发送的指令。
 * 在实际应用中，这里会是WebSocket的onmessage事件监听器，
 * 或一个定时器轮询Java后端API。
 */
chrome.runtime.onMessage.addListener(async (message, sender, sendResponse) => {
    if (message.command === "perform_baidu_search") {
        console.log("Received command from backend (simulated): Perform Baidu search.");

        // 1. 打开或切换到百度页面
        let tabId;
        const tabs = await chrome.tabs.query({ url: BAIDU_URL });
        if (tabs.length > 0) {
            tabId = tabs[0].id;
            await chrome.tabs.update(tabId, { active: true, url: BAIDU_URL });
            console.log(`Switched to existing Baidu tab: ${tabId}`);
        } else {
            const newTab = await chrome.tabs.create({ url: BAIDU_URL });
            tabId = newTab.id;
            console.log(`Created new Baidu tab: ${tabId}`);
        }

        // 确保页面加载完成，通常监听onUpdated事件
        // 为了简单，这里直接等待一小段时间，实际生产中应监听tab.onUpdated事件的complete状态
        await new Promise(resolve => setTimeout(resolve, 2000));

        try {
            // 2. 向内容脚本发送搜索指令
            // 检查内容脚本是否已注入，如果未注入则先注入
            await chrome.scripting.executeScript({
                target: { tabId: tabId },
                files: ['content.js']
            });
            console.log(`Content script injected into tab ${tabId}.`);

            const response = await chrome.tabs.sendMessage(tabId, {
                action: "search_baidu",
                query: SEARCH_QUERY
            });

            console.log("Search results received from content script:", response);

            // 3. 将结果发送回Java后端
            if (response && response.status === "success" && response.results) {
                await sendResultsToBackend(response.results);
                sendResponse({ status: "success", message: "Search completed and results sent to backend." });
            } else {
                console.error("Failed to get search results from content script.");
                sendResponse({ status: "error", message: "Failed to get search results." });
            }

        } catch (error) {
            console.error("Error during Baidu search automation:", error);
            sendResponse({ status: "error", message: error.message });
        }
    }
});

// 辅助函数：将结果发送到Java后端
async function sendResultsToBackend(results) {
    try {
        const response = await fetch(JAVA_BACKEND_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(results)
        });

        if (response.ok) {
            console.log("Search results successfully sent to Java backend.");
        } else {
            console.error("Failed to send search results to Java backend:", response.status, response.statusText);
            const errorText = await response.text();
            console.error("Backend error response:", errorText);
        }
    } catch (error) {
        console.error("Error sending results to Java backend:", error);
    }
}

// 示例：通过插件popup触发，或通过WebSocket连接模拟后台指令
// 这段代码是为 popup.html 准备的，点击按钮会触发 `perform_baidu_search` 指令
// 在实际后台发送指令时，这部分逻辑会被 WebSocket 的 onmessage 事件处理。
// chrome.runtime.onConnect.addListener((port) => {
//   if (port.name === "popup_channel") {
//     port.onMessage.addListener((msg) => {
//       if (msg.command === "start_search") {
//         chrome.runtime.sendMessage({ command: "perform_baidu_search" });
//       }
//     });
//   }
// });