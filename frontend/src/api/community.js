import request from '@/utils/request'

export function getCommunityRecords(username) {
    return request.get('/community/records', {
        params: {username}
    })
}

export function getRecordDetail(recordId) {
    return request.get(`/community/records/${recordId}`)
}

export function likeRecord(recordId, username) {
    return request.post(`/community/records/${recordId}/like`, null, {
        params: { username }
    })
}

export function unlikeRecord(recordId, username) {
    return request.delete(`/community/records/${recordId}/like`, {
        params: { username }
    })
}

export function createComment(recordId, username, content, parentCommentId = null) {
    return request.post(`/community/records/${recordId}/comments`, null, {
        params: {
            username,
            content,
            parentCommentId
        }
    })
}

export function deleteComment(commentId, userId) {
    return request.delete(`/community/comments/${commentId}`, {
        params: { userId }
    })
}

export function getRecordComments(recordId) {
    return request.get(`/community/records/${recordId}/comments`)
}

export function shareRecord(recordId, userId) {
    return request.post(`/community/records/${recordId}/share`, null, {
        params: { userId }
    })
}

export function unshareRecord(recordId, userId) {
    return request.delete(`/community/records/${recordId}/share`, {
        params: { userId }
    })
}