import request from "@/utils/request";

export function getCustomizeUnreadCount() {
  return request({
    url: "/api/customize/unread-count",
    method: "get",
  });
}

export function getMerchantList(params) {
  return request({
    url: "/api/customize/merchants",
    method: "get",
    params,
  });
}

export function pageMerchantProducts(merchantId, params) {
  return request({
    url: `/api/customize/merchants/${merchantId}/products`,
    method: "get",
    params,
  });
}

export function createCustomizeRequest(data) {
  return request({
    url: "/api/customize/requests",
    method: "post",
    data,
  });
}

export function pageMyRequests(params) {
  return request({
    url: "/api/customize/requests/mine",
    method: "get",
    params,
  });
}

export function pageReceivedRequests(params) {
  return request({
    url: "/api/customize/requests/received",
    method: "get",
    params,
  });
}

export function getCustomizeRequestDetail(id) {
  return request({
    url: `/api/customize/requests/${id}`,
    method: "get",
  });
}

export function quoteCustomizeRequest(id, data) {
  return request({
    url: `/api/customize/requests/${id}/quote`,
    method: "put",
    data,
  });
}

export function confirmCustomizeRequest(id, data) {
  return request({
    url: `/api/customize/requests/${id}/confirm`,
    method: "put",
    data,
  });
}

export function updateCustomizeRequestStatus(id, data) {
  return request({
    url: `/api/customize/requests/${id}/status`,
    method: "put",
    data,
  });
}

export function sendCustomizeMessage(requestId, data) {
  return request({
    url: `/api/customize/requests/${requestId}/messages`,
    method: "post",
    data,
  });
}

export function listCustomizeMessages(requestId) {
  return request({
    url: `/api/customize/requests/${requestId}/messages`,
    method: "get",
  });
}

export function markMessagesRead(requestId) {
  return request({
    url: `/api/customize/requests/${requestId}/messages/read`,
    method: "put",
  });
}

/**
 * 用户标记已完成工单已读
 */
export function markCompletedRead(id) {
  return request({
    url: `/api/customize/requests/${id}/completed-read`,
    method: "put",
  });
}

/**
 * 商家标记已取消工单已读
 */
export function markCancelledRead(id) {
  return request({
    url: `/api/customize/requests/${id}/cancelled-read`,
    method: "put",
  });
}
