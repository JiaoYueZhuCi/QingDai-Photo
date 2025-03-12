window.onload = function() {
    const jwt = 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyb290IiwidXNlcklkIjoxLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NDE3NTY2ODJ9.PwqvDp0P4nx4eQSPm5Lv7Ng7zu9Bi1zIK7xnyRt9izI'; // 注意这里需要动态替换

    const ui = SwaggerUIBundle({
        url: "/v3/api-docs",
        dom_id: '#swagger-ui',
        presets: [
            SwaggerUIBundle.presets.apis,
            SwaggerUIStandalonePreset
        ],
        plugins: [
            SwaggerUIBundle.plugins.DownloadUrl
        ],
        layout: "StandaloneLayout",
        requestInterceptor: function(req) {
            // 自动添加 Authorization 头
            if (!req.headers.Authorization) {
                req.headers.Authorization = jwt;
            }
            return req;
        }
    });
};