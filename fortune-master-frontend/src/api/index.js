const API_BASE_URL = import.meta.env.PROD ? '/api' : 'http://localhost:8123/api'

export const connectSSE = (url, params = {}, onMessage, onError) => {
  const queryString = Object.entries(params)
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&')
  const fullUrl = `${API_BASE_URL}${url}?${queryString}`
  const eventSource = new EventSource(fullUrl)

  eventSource.onmessage = (event) => {
    onMessage?.(event.data)
  }

  eventSource.onerror = (error) => {
    onError?.(error)
    eventSource.close()
  }

  return eventSource
}

export const chatWithFortuneApp = (message, chatId) =>
  connectSSE('/ai/fortune_app/chat/sse', { message, chatId })
