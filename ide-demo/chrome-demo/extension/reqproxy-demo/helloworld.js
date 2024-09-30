function helloworld() {
    alert('Hello World')
}

setTimeout(() => {
    document.getElementById('helloworldButton').addEventListener('click', () => {
        chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
            chrome.scripting.executeScript({
                target: { tabId: tabs[0].id },
                function: helloworld,
            })
        })
    })
}, 500)

