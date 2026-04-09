import request from "@/utils/request";

export function getProductComments(productId, params) {
  return request({
    url: `/api/shop/products/${productId}/comments`,
    method: "get",
    params,
  });
}

export function getCommentReplies(commentId, params) {
  return request({
    url: `/api/shop/comments/${commentId}/replies`,
    method: "get",
    params,
  });
}

export function createComment(data) {
  return request({
    url: "/api/comments",
    method: "post",
    data,
  });
}

export function deleteComment(id) {
  return request({
    url: `/api/comments/${id}`,
    method: "delete",
  });
}

export function toggleCommentLike(id) {
  return request({
    url: `/api/comments/${id}/like`,
    method: "post",
  });
}
