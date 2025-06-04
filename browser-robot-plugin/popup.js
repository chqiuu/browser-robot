// popup.js
document.getElementById('startSearch').addEventListener('click', () => {
    // 向后台服务工作者发送消息，模拟后台指令
    chrome.runtime.sendMessage({ command: "perform_baidu_search" });
    window.close(); // 关闭popup
});