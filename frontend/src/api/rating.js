import request from "@/utils/request";

export function getProductRating(productId) {
  return request({
    url: `/api/shop/products/${productId}/rating`,
    method: "get",
  });
}

export function submitRating(data) {
  return request({
    url: "/api/ratings",
    method: "post",
    data,
  });
}

export function getMyRating(productId) {
  return request({
    url: "/api/ratings/my",
    method: "get",
    params: { productId },
  });
}
