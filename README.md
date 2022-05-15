# Stock_analyzer

### Step to run

1. Download Google Chrome driver: https://chromedriver.chromium.org/downloads
2. Add maven wrapper: ``mvn wrapper:wrapper``
3. Fill application-local.yaml with your data:
    1. Create telegram-bot using @BotFather
    2. Copy token and bot's username to properties
    3. Download **ngrok**: https://dashboard.ngrok.com/get-started/setup
    4. Run server and add "Forwarding" link to webhookPath and server port
    5. Also add account's phone number and username to appropriate properties
4. Send query: https://api.telegram.org/bot<token\>/setWebhook?url=<webhookPath\>
