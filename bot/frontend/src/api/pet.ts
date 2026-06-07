import { get, post, put, del } from './request'
import type { Pet, PetCreateRequest, PetUpdateRequest, PageData } from '@/types/api'

export function getPets(params?: { page?: number; page_size?: number }) {
  return get<PageData<Pet>>('/pets', params)
}

export function getPet(id: number) {
  return get<Pet>(`/pets/${id}`)
}

export function createPet(data: PetCreateRequest) {
  return post<Pet>('/pets', data)
}

export function updatePet(id: number, data: PetUpdateRequest) {
  return put<Pet>(`/pets/${id}`, data)
}

export function deletePet(id: number) {
  return del(`/pets/${id}`)
}
