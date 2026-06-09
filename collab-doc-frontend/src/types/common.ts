export interface ApiResponse<T> {
  success: boolean
  data: T
  message: string | null
}

export interface ErrorResponse {
  success: boolean
  message: string
  errors: Record<string, string> | null
}
