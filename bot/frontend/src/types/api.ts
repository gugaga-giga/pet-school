// 通用API响应
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页数据
export interface PageData<T = any> {
  items: T[]
  total: number
  page: number
  page_size: number
}

// 认证相关
export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  nickname?: string
}

export interface TokenResponse {
  access_token: string
  refresh_token: string
  token_type: string
}

// 用户相关
export interface UserInfo {
  id: number
  username: string
  email: string
  nickname: string
  avatar: string
  phone: string
  status: number
  roles: string[]
  permissions: string[]
  created_at: string
  updated_at: string
}

export interface UserUpdateRequest {
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
}

export interface UserListParams {
  page?: number
  page_size?: number
  keyword?: string
  status?: number
}

// 知识库相关
export interface KnowledgeBase {
  id: number
  name: string
  description: string
  doc_count: number
  chunk_count: number
  embedding_model: string
  status: number
  created_at: string
  updated_at: string
}

export interface KnowledgeBaseCreate {
  name: string
  description: string
  embedding_model?: string
}

export interface KnowledgeBaseUpdate {
  name?: string
  description?: string
}

// 文档相关
export interface Document {
  id: number
  knowledge_base_id: number
  filename: string
  file_type: string
  file_size: number
  chunk_count: number
  status: 'pending' | 'processing' | 'completed' | 'failed'
  error_message?: string
  created_at: string
  updated_at: string
}

export interface DocumentChunk {
  id: number
  document_id: number
  content: string
  chunk_index: number
  metadata: Record<string, any>
}

export interface DocumentUploadParams {
  knowledge_base_id: number
  files: File[]
}

// 聊天相关
export interface ChatSession {
  id: number
  title: string
  knowledge_base_id?: number
  knowledge_base_name?: string
  message_count: number
  created_at: string
  updated_at: string
}

export interface ChatMessage {
  id: number
  session_id: number
  role: 'user' | 'assistant' | 'system'
  content: string
  intent?: string
  sources?: SourceItem[]
  products?: ProductItem[]
  tokens?: number
  created_at: string
}

export interface SourceItem {
  document_id: number
  document_name: string
  chunk_id: number
  content: string
  score: number
}

export interface ProductItem {
  id: number
  name: string
  description: string
  price: number
  image?: string
  category: string
}

export interface ChatRequest {
  session_id: number
  message: string
  knowledge_base_id?: number
  stream?: boolean
}

export interface StreamChunk {
  type: 'content' | 'sources' | 'products' | 'intent' | 'done'
  data: any
}

export interface SessionCreateRequest {
  title?: string
  knowledge_base_id?: number
}

// 宠物相关
export interface Pet {
  id: number
  user_id: number
  nickname: string
  pet_type: 'dog' | 'cat' | 'bird' | 'fish' | 'hamster' | 'rabbit' | 'other'
  breed: string
  birth_date: string
  weight: number
  gender: 'male' | 'female'
  is_neutered: boolean
  health_status: string
  vaccine_status: string
  avatar?: string
  created_at: string
  updated_at: string
}

export interface PetCreateRequest {
  nickname: string
  pet_type: string
  breed: string
  birth_date: string
  weight: number
  gender: string
  is_neutered?: boolean
  health_status?: string
  vaccine_status?: string
  avatar?: string
}

export interface PetUpdateRequest {
  nickname?: string
  pet_type?: string
  breed?: string
  birth_date?: string
  weight?: number
  gender?: string
  is_neutered?: boolean
  health_status?: string
  vaccine_status?: string
  avatar?: string
}

// 管理后台相关
export interface DashboardStats {
  user_count: number
  knowledge_base_count: number
  document_count: number
  session_count: number
  today_sessions: number
  today_messages: number
}

export interface TrendData {
  dates: string[]
  session_counts: number[]
  message_counts: number[]
}

export interface IntentDistribution {
  name: string
  value: number
}

export interface QALog {
  id: number
  session_id: number
  user_message: string
  assistant_message: string
  intent: string
  sources_count: number
  tokens: number
  response_time: number
  created_at: string
}

export interface QALogParams {
  page?: number
  page_size?: number
  intent?: string
  start_date?: string
  end_date?: string
  keyword?: string
}

export interface SystemSettings {
  llm_model: string
  llm_temperature: number
  llm_max_tokens: number
  rag_chunk_size: number
  rag_chunk_overlap: number
  rag_top_k: number
  rag_score_threshold: number
  system_prompt: string
}

export interface SystemSettingsUpdate {
  llm_model?: string
  llm_temperature?: number
  llm_max_tokens?: number
  rag_chunk_size?: number
  rag_chunk_overlap?: number
  rag_top_k?: number
  rag_score_threshold?: number
  system_prompt?: string
}
