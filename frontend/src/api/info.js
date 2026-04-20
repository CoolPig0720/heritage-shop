import request from "@/utils/request";

export function getAnnouncementPage(params) {
  return request({
    url: "/api/info/announcements",
    method: "get",
    params,
  });
}

export function getAnnouncementDetail(id) {
  return request({
    url: `/api/info/announcements/${id}`,
    method: "get",
  });
}

export function getHeritageStoryPage(params) {
  return request({
    url: "/api/info/heritage-stories",
    method: "get",
    params,
  });
}

export function getHeritageStoryDetail(id) {
  return request({
    url: `/api/info/heritage-stories/${id}`,
    method: "get",
  });
}
