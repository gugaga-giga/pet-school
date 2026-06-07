"""宠物服务模块，处理宠物CRUD和统计 - 匹配Pet ORM模型"""

from typing import Optional

from sqlalchemy import func
from sqlalchemy.orm import Session

from app.core.exceptions import NotFoundException
from app.models.pet import Pet
from app.schemas.pet import PetCreate, PetUpdate
from app.utils.pagination import paginate


class PetService:
    """宠物服务类"""

    @staticmethod
    def get_pet_by_id(db: Session, pet_id: int) -> Pet:
        pet = db.query(Pet).filter(Pet.id == pet_id, Pet.is_deleted == 0).first()
        if not pet:
            raise NotFoundException(message="宠物不存在")
        return pet

    @staticmethod
    def get_pet_list(
        db: Session,
        page: int = 1,
        page_size: int = 10,
        pet_type: Optional[str] = None,
        user_id: Optional[int] = None,
        keyword: Optional[str] = None,
    ) -> dict:
        query = db.query(Pet).filter(Pet.is_deleted == 0)

        if pet_type:
            query = query.filter(Pet.type == pet_type)

        if user_id:
            query = query.filter(Pet.user_id == user_id)

        if keyword:
            query = query.filter(
                (Pet.name.like(f"%{keyword}%"))
                | (Pet.breed.like(f"%{keyword}%"))
            )

        query = query.order_by(Pet.created_at.desc())
        return paginate(query, page, page_size)

    @staticmethod
    def create_pet(db: Session, pet_data: PetCreate, user_id: Optional[int] = None) -> Pet:
        data = pet_data.model_dump()
        if user_id:
            data["user_id"] = user_id
        pet = Pet(**data)
        db.add(pet)
        db.commit()
        db.refresh(pet)
        return pet

    @staticmethod
    def update_pet(db: Session, pet_id: int, pet_data: PetUpdate) -> Pet:
        pet = PetService.get_pet_by_id(db, pet_id)
        update_fields = pet_data.model_dump(exclude_unset=True)
        for field, value in update_fields.items():
            setattr(pet, field, value)
        db.commit()
        db.refresh(pet)
        return pet

    @staticmethod
    def delete_pet(db: Session, pet_id: int) -> None:
        pet = PetService.get_pet_by_id(db, pet_id)
        pet.is_deleted = 1
        db.commit()

    @staticmethod
    def get_statistics(db: Session) -> dict:
        total = db.query(Pet).filter(Pet.is_deleted == 0).count()
        # 按类型统计
        type_stats = (
            db.query(Pet.type, func.count(Pet.id))
            .filter(Pet.is_deleted == 0)
            .group_by(Pet.type)
            .all()
        )
        return {
            "total": total,
            "by_type": {t: count for t, count in type_stats},
        }
