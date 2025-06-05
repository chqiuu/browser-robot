// background.js

let ws;
// **重要**：请根据你的Spring Boot WebSocket实际地址修改此URL
const SPRINGBOOT_WEBSOCKET_URL = "ws://localhost:8080/ws/browser-control";

// 存储当前正在处理的Baidu标签页ID，确保只在一个Baidu标签页上操作
let currentBaiduTabId = null;

function connectWebSocket() {
    if (ws && ws.readyState === WebSocket.OPEN) {
        console.log("WebSocket已连接.");
        return;
    }

    console.log("尝试连接WebSocket到Spring Boot后端...");
    ws = new WebSocket(SPRINGBOOT_WEBSOCKET_URL);

    ws.onopen = () => {
        console.log("WebSocket连接到Spring Boot后端成功。");
        // 连接成功后，可以向后端发送一个“浏览器已准备好”的消息
        ws.send(JSON.stringify({ type: "browser_ready", message: "Browser extension connected and ready." }));
    };

    ws.onmessage = async (event) => {
        const message = JSON.parse(event.data);
        console.log("从后端接收到消息:", message);

        if (message.command === "openBaiduAndSearch") {
            const query = message.query;
            let tabIdToUse;

            // 1. 查找或创建Baidu标签页
            const tabs = await chrome.tabs.query({ url: "*://www.baidu.com/*" });
            if (tabs.length > 0) {
                tabIdToUse = tabs[0].id;
                await chrome.tabs.update(tabIdToUse, { active: true, url: "https://www.baidu.com/" });
                console.log(`更新并激活现有Baidu标签页 ${tabIdToUse}，导航至baidu.com`);
            } else {
                const tab = await chrome.tabs.create({ url: "https://www.baidu.com/", active: true });
                tabIdToUse = tab.id;
                console.log(`创建新的Baidu标签页 ${tabIdToUse}`);
            }
            currentBaiduTabId = tabIdToUse; // 记录当前操作的Baidu标签页ID

            // 2. 等待标签页加载完成
            // 这是一个简单的延时，更健壮的方案是使用 chrome.webNavigation.onCompleted 事件
            // 或者让 content_script 在 DOM ready 时发送消息
            setTimeout(async () => {
                try {
                    // 3. 在目标标签页中注入并执行 content_script.js
                    // Manifest V3 需要显式执行 content_script
                    await chrome.scripting.executeScript({
                        target: { tabId: tabIdToUse },
                        files: ['content_script.js']
                    });
                    console.log(`已将 content_script.js 注入到标签页 ${tabIdToUse}`);

                    // 4. 向内容脚本发送搜索指令
                    // 传递 search command 和 query
                    const response = await chrome.tabs.sendMessage(tabIdToUse, { command: "search", query: query });
                    console.log("从内容脚本接收到响应 (初始确认):", response);

                } catch (e) {
                    console.error("执行或与内容脚本通信时出错:", e);
                    if (ws && ws.readyState === WebSocket.OPEN) {
                        ws.send(JSON.stringify({ type: "error", message: `浏览器插件执行错误: ${e.message}` }));
                    }
                }
            }, 3000); // 3秒延时，等待页面加载，可能需要根据网络情况调整
        }
    };

    ws.onclose = (event) => {
        console.warn("WebSocket连接已断开. 代码:", event.code, "原因:", event.reason);
        // 连接断开后尝试重连
        setTimeout(connectWebSocket, 5000);
    };

    ws.onerror = (error) => {
        console.error("WebSocket错误:", error);
        ws.close(); // 关闭以触发 onclose 处理器进行重连
    };
}

// 监听来自内容脚本的消息
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    // 确保是来自我们当前操作的Baidu标签页
    if (sender.tab && sender.tab.id === currentBaiduTabId) {
        if (message.type === "search_results") {
            console.log("从内容脚本接收到搜索结果:", message.results);
            // 将搜索结果以JSON格式发送回Spring Boot后端
            if (ws && ws.readyState === WebSocket.OPEN) {
                ws.send(JSON.stringify({ type: "search_results", results: message.results }));
            } else {
                console.error("WebSocket未连接或已关闭，无法发送搜索结果到后端。");
            }
            sendResponse({ status: "results_received_by_background" });
        } else if (message.type === "error") {
            console.error("从内容脚本接收到错误:", message.message);
            if (ws && ws.readyState === WebSocket.OPEN) {
                ws.send(JSON.stringify({ type: "error", message: `内容脚本错误: ${message.message}` }));
            }
            sendResponse({ status: "error_received_by_background" });
        }
    }
    return true; // 返回 true 以指示我们将异步发送响应
});

// 服务工作者启动时连接WebSocket
connectWebSocket();

// 为了防止服务工作者在不活跃时被终止，可以考虑使用 keepAlive 机制，
// 但对于此场景，WebSocket的onclose重连机制通常足够。