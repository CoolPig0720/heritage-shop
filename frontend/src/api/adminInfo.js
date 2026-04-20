import request from "@/utils/request";

export function createAnnouncement(data) {
  return request({
    url: "/api/admin/announcements",
    method: "post",
    data,
  });
}

export function updateAnnouncement(id, data) {
  return request({
    url: `/api/admin/announcements/${id}`,
    method: "put",
    data,
  });
}

export function deleteAnnouncement(id) {
  return request({
    url: `/api/admin/announcements/${id}`,
    method: "delete",
  });
}

export function pageAdminAnnouncements(params) {
  return request({
    url: "/api/admin/announcements",
    method: "get",
    params,
  });
}

export function getAdminAnnouncementDetail(id) {
  return request({
    url: `/api/admin/announcements/${id}`,
    method: "get",
  });
}

export function createHeritageStory(data) {
  return request({
    url: "/api/admin/heritage-stories",
    method: "post",
    data,
  });
}

export function updateHeritageStory(id, data) {
  return request({
    url: `/api/admin/heritage-stories/${id}`,
    method: "put",
    data,
  });
}

export function deleteHeritageStory(id) {
  return request({
    url: `/api/admin/heritage-stories/${id}`,
    method: "delete",
  });
}

export function pageAdminHeritageStories(params) {
  return request({
    url: "/api/admin/heritage-stories",
    method: "get",
    params,
  });
}

export function getAdminHeritageStoryDetail(id) {
  return request({
    url: `/api/admin/heritage-stories/${id}`,
    method: "get",
  });
}
