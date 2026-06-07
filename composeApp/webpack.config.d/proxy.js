// Proxy API requests to the Ktor backend running on port 8080
config.devServer = config.devServer || {};
config.devServer.historyApiFallback = true;
config.devServer.proxy = [
  {
    context: [
      "/auth",
      "/categories",
      "/crew-members",
      "/customers",
      "/discounts",
      "/orders",
      "/payments",
      "/products",
      "/public",
      "/tables",
      "/uploads",
    ],
    target: "http://localhost:8081",
    changeOrigin: true,
  },
];
