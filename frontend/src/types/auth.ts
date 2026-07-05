export type UserRole = 'STUDENT' | 'TEACHER' | 'ADMIN'

export interface LoginDTO {
  username: string
  password: string
  role: UserRole
}

export interface LoginVO {
  userId: number
  relatedId: number
  username: string
  role: UserRole
  name: string
}
