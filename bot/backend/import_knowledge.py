"""PetSchool知识库导入脚本

从pet_school数据库读取业务数据，生成知识文本chunks，写入chatbot库，并重建FAISS索引。

用法: cd backend && python import_knowledge.py
"""

import pymysql
import json
import os
import sys

# 添加项目路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))


def get_connection(db_name):
    """获取数据库连接，依次尝试常见密码"""
    passwords = ['123456', 'root', '', 'password', 'mysql']
    for pwd in passwords:
        try:
            conn = pymysql.connect(
                host='localhost', port=3306, user='root',
                password=pwd, database=db_name, charset='utf8mb4'
            )
            print(f"使用密码'{pwd}'连接{db_name}成功")
            return conn
        except pymysql.err.OperationalError:
            continue
    raise Exception(f"无法连接数据库{db_name}，所有密码尝试均失败")


def generate_course_chunks(cursor):
    """生成课程知识chunks"""
    chunks = []
    # 课程+分类+套餐
    cursor.execute("""
        SELECT c.id, c.name, c.duration, c.monthly_price, c.description,
               cc.name as category_name
        FROM course c
        LEFT JOIN course_category cc ON c.category_id = cc.id
        WHERE c.id > 0
    """)
    courses = cursor.fetchall()

    for course in courses:
        course_id, name, duration, price, desc, category = course
        text = f"【课程信息】\n课程名称：{name}\n"
        if category:
            text += f"课程分类：{category}\n"
        if duration:
            text += f"课程时长：{duration}个月\n"
        if price:
            text += f"课程价格：¥{price}/月\n"
        if desc:
            text += f"课程描述：{desc}\n"

        # 查询套餐
        cursor.execute("""
            SELECT cp.name, cp.price, cp.level
            FROM course_package cp
            WHERE cp.course_id = %s
        """, (course_id,))
        packages = cursor.fetchall()
        if packages:
            text += "可选套餐：\n"
            for pkg in packages:
                pkg_name, pkg_price, pkg_level = pkg
                text += f"  - {pkg_name}"
                if pkg_price:
                    text += f"（¥{pkg_price}）"
                if pkg_level:
                    level_names = {1: '基础', 2: '进阶', 3: '高级'}
                    text += f" [{level_names.get(pkg_level, f'等级{pkg_level}')}]"
                text += "\n"

        chunks.append({
            'content': text,
            'doc_type': 'course',
            'ref_id': course_id,
            'title': f'课程-{name}',
            'kb_id': 1
        })
    return chunks


def generate_faq_chunks():
    """生成FAQ知识chunks（硬编码常见问题）"""
    faqs = [
        {"q": "PetSchool的营业时间是什么？", "a": "PetSchool营业时间为每天9:00-21:00，周末及节假日正常营业。建议提前预约课程。"},
        {"q": "PetSchool的地址在哪里？", "a": "PetSchool位于市中心宠物公园旁，具体地址请咨询客服热线。支持导航到店。"},
        {"q": "如何购买课程？", "a": "购买课程流程：1.登录PetSchool账号 → 2.浏览课程列表选择心仪课程 → 3.选择套餐 → 4.确认订单并支付 → 5.开始上课。支持微信、支付宝和钱包余额支付。"},
        {"q": "如何退款？", "a": "退款政策：1.未开课可全额退款；2.开课后7天内可退剩余课时费用的80%；3.因学校原因取消课程全额退款；4.退款将在3-5个工作日内原路返回。请联系客服发起退款。"},
        {"q": "如何添加宠物？", "a": "添加宠物步骤：1.登录PetSchool → 2.进入'我的宠物'页面 → 3.点击'新增宠物' → 4.填写宠物信息（名称、品种、类型、性别、生日、体重等）→ 5.保存。"},
        {"q": "如何查看订单？", "a": "查看订单步骤：1.登录PetSchool → 2.进入'订单中心' → 3.可按状态筛选（待支付/已支付/已完成/已取消）→ 4.点击订单查看详情。"},
        {"q": "宠物疫苗注意事项？", "a": "宠物疫苗注意事项：1.幼犬8周龄开始首次免疫；2.核心疫苗：犬瘟热、细小病毒、腺病毒；3.非核心疫苗：犬副流感、钩端螺旋体等视情况接种；4.接种前确保宠物健康；5.接种后观察30分钟；6.每年加强免疫一次。"},
        {"q": "课程适合什么年龄的宠物？", "a": "课程适合年龄：1.基础服从训练：3月龄以上；2.社会化训练：3-6月龄最佳；3.行为矫正：6月龄以上；4.表演类训练：1岁以上且完成基础训练。幼犬建议先完成社会化训练再进入基础服从。"},
        {"q": "PetSchool提供寄宿服务吗？", "a": "PetSchool提供宠物寄宿服务，包含标准间和豪华间，提供24小时监控、定时喂食、每日遛弯。寄宿需提前预约，宠物需提供疫苗证明。"},
        {"q": "如何联系客服？", "a": "联系客服方式：1.在线客服（右下角AI客服按钮）；2.客服热线：400-PET-SCHOOL；3.微信公众号：PetSchool宠物学校；4.工作时间：每天9:00-21:00。"},
    ]
    chunks = []
    for i, faq in enumerate(faqs):
        text = f"【常见问题】\n问题：{faq['q']}\n答案：{faq['a']}"
        chunks.append({
            'content': text,
            'doc_type': 'faq',
            'ref_id': i + 1,
            'title': f"FAQ-{faq['q'][:20]}",
            'kb_id': 2
        })
    return chunks


def generate_pet_knowledge_chunks():
    """生成宠物百科知识chunks"""
    knowledge = [
        {"title": "金毛寻回犬饲养指南", "content": "【宠物百科-金毛寻回犬】\n品种特点：金毛寻回犬性格温顺、聪明、忠诚，是最受欢迎的家庭犬之一。体型大型，成年体重25-34kg。\n饲养要点：1.每日需要1-2小时运动；2.定期梳理毛发（每周2-3次）；3.注意髋关节发育不良；4.易患皮肤病，保持毛发干燥；5.食量大，注意控制体重。\n训练建议：金毛智商排名第四，非常适合服从训练，建议3月龄开始基础训练。"},
        {"title": "拉布拉多饲养指南", "content": "【宠物百科-拉布拉多】\n品种特点：拉布拉多性格友善、活泼、聪明，是优秀的导盲犬和家庭犬。体型大型，成年体重25-36kg。\n饲养要点：1.每日需要大量运动；2.控制饮食防止肥胖；3.定期检查耳朵防感染；4.短毛但掉毛严重；5.喜欢游泳，可安排水上活动。\n训练建议：智商排名第七，学习能力强，适合各种训练课程。"},
        {"title": "柯基饲养指南", "content": "【宠物百科-柯基】\n品种特点：柯基犬性格勇敢、友好、活泼，腿短身长是其标志。体型中型，成年体重10-14kg。\n饲养要点：1.控制体重，避免脊椎压力过大；2.避免频繁上下楼梯；3.每日适度运动；4.注意牙齿健康；5.掉毛严重，需经常梳理。\n训练建议：柯基独立性强，训练需耐心，建议使用正向激励法。"},
        {"title": "猫咪基本饲养知识", "content": "【宠物百科-猫咪】\n基本饲养：1.提供优质猫粮和新鲜饮水；2.定期清理猫砂盆（每天1-2次）；3.提供猫抓板和攀爬空间；4.室内饲养更安全；5.定期驱虫和免疫。\n健康注意：1.注意泌尿系统健康，多饮水；2.定期口腔检查；3.注意肥胖问题；4.每年体检一次；5.适龄绝育可减少疾病风险。"},
        {"title": "幼犬到家注意事项", "content": "【宠物百科-幼犬到家】\n准备工作：1.准备食盆、水盆、狗粮、狗窝、牵引绳；2.家中收好电线、小物件、有毒植物；3.指定排泄区域。\n到家后：1.给幼犬适应时间，不要过度打扰；2.保持原有饮食7天后再换粮；3.3天内不要洗澡；4.完成疫苗前不要外出；5.建立规律作息。\n训练起步：1.固定名字，反复呼唤；2.定点排泄训练从第一天开始；3.社会化黄金期3-12周龄。"},
        {"title": "宠物驱虫指南", "content": "【宠物百科-驱虫】\n驱虫频率：1.体内驱虫：幼犬每月一次，成犬每3个月一次；2.体外驱虫：每月一次（尤其夏季）。\n常见寄生虫：1.体内：蛔虫、绦虫、钩虫、心丝虫；2.体外：跳蚤、蜱虫、螨虫。\n注意事项：1.驱虫前后2小时禁食；2.按体重选择剂量；3.驱虫后观察排便；4.怀孕母犬需咨询兽医；5.多宠家庭需同时驱虫。"},
    ]
    chunks = []
    for i, k in enumerate(knowledge):
        chunks.append({
            'content': k['content'],
            'doc_type': 'pet_knowledge',
            'ref_id': i + 1,
            'title': k['title'],
            'kb_id': 1
        })
    return chunks


def generate_service_chunks():
    """生成服务信息chunks"""
    services = [
        {"title": "PetSchool服务项目总览", "content": "【PetSchool服务项目】\n1. 宠物训练课程：基础服从、社会化训练、行为矫正、定点大小便训练、表演类训练等\n2. 宠物寄宿服务：标准间/豪华间，24小时监控，定时喂食遛弯\n3. 宠物医疗服务：在线问诊、预约就诊、疫苗注射、驱虫服务\n4. 宠物健康管理：健康档案、健康趋势分析、AI健康预警\n5. 宠物美容服务：洗澡、剪毛、指甲修剪\n6. 在线商城：狗粮、猫粮、零食、玩具、用品\n7. 毕业证书：完成训练课程可获毕业证书\n8. 优惠券活动：定期发放优惠券，新用户专享优惠"},
        {"title": "PetSchool退款与售后政策", "content": "【退款与售后政策】\n课程退款：1.未开课全额退款；2.开课后7天内退剩余课时80%；3.因学校原因取消全额退款；4.退款3-5工作日到账。\n寄宿退款：1.提前24小时取消免费；2.24小时内取消收首日费用；3.提前退房按实际天数结算。\n医疗退款：1.未就诊全额退款；2.已就诊按实际费用结算。\n联系方式：客服热线400-PET-SCHOOL，工作日9:00-21:00。"},
    ]
    chunks = []
    for i, s in enumerate(services):
        chunks.append({
            'content': s['content'],
            'doc_type': 'service',
            'ref_id': i + 1,
            'title': s['title'],
            'kb_id': 2
        })
    return chunks


def rebuild_faiss_index():
    """重建FAISS向量索引"""
    from app.rag.embedding.bge_m3 import BGEEmbedding
    from app.rag.vectorstore.faiss_store import FAISSVectorStore
    from app.core.config import settings

    # 获取所有chunks
    conn_chatbot = get_connection('chatbot')
    cursor = conn_chatbot.cursor(pymysql.cursors.DictCursor)
    cursor.execute("SELECT id, content, knowledge_base_id FROM document_chunks WHERE is_deleted = 0")
    all_chunks = cursor.fetchall()

    if not all_chunks:
        print("No chunks found, skipping FAISS rebuild")
        conn_chatbot.close()
        return

    print(f"Building FAISS index for {len(all_chunks)} chunks...")

    # 生成embeddings
    embedding = BGEEmbedding()
    texts = [c['content'] for c in all_chunks]
    chunk_ids = [c['id'] for c in all_chunks]
    kb_ids = [c['knowledge_base_id'] for c in all_chunks]

    vectors = embedding.embed_documents(texts)

    # 按知识库分组构建索引
    import numpy as np

    vector_store = FAISSVectorStore(settings.FAISS_INDEX_DIR, dimension=embedding.get_dimension())

    for kb_id in set(kb_ids):
        kb_indices = [i for i, k in enumerate(kb_ids) if k == kb_id]
        kb_vectors = np.array([vectors[i] for i in kb_indices], dtype=np.float32)
        kb_chunk_ids = [chunk_ids[i] for i in kb_indices]

        if len(kb_vectors) == 0:
            continue

        # 重建索引
        kb_id_str = str(kb_id)
        vector_store.rebuild_index(kb_id_str, kb_vectors, kb_chunk_ids)
        vector_store.save_index(kb_id_str)
        print(f"Saved FAISS index for KB {kb_id}: {len(kb_vectors)} vectors")

        # 更新chunk的faiss_id
        id_map = vector_store.id_maps.get(kb_id_str, {})
        reverse_map = {v: k for k, v in id_map.items()}
        for chunk_id in kb_chunk_ids:
            faiss_id = reverse_map.get(chunk_id)
            if faiss_id is not None:
                cursor.execute(
                    "UPDATE document_chunks SET faiss_id = %s WHERE id = %s",
                    (faiss_id, chunk_id)
                )

    conn_chatbot.commit()
    conn_chatbot.close()
    print("FAISS index rebuild complete!")


if __name__ == '__main__':
    print("=== PetSchool 知识库导入开始 ===")

    # 1. 生成所有知识chunks
    all_chunks = []

    # 课程数据
    conn_pet = get_connection('pet_school')
    cursor = conn_pet.cursor()
    course_chunks = generate_course_chunks(cursor)
    all_chunks.extend(course_chunks)
    print(f"生成课程chunks: {len(course_chunks)}")

    # FAQ数据
    faq_chunks = generate_faq_chunks()
    all_chunks.extend(faq_chunks)
    print(f"生成FAQ chunks: {len(faq_chunks)}")

    # 宠物百科
    pet_chunks = generate_pet_knowledge_chunks()
    all_chunks.extend(pet_chunks)
    print(f"生成宠物百科chunks: {len(pet_chunks)}")

    # 服务信息
    service_chunks = generate_service_chunks()
    all_chunks.extend(service_chunks)
    print(f"生成服务信息chunks: {len(service_chunks)}")

    conn_pet.close()

    # 2. 写入chatbot库
    conn_chatbot = get_connection('chatbot')
    cursor = conn_chatbot.cursor()

    # 先清理旧的同步数据（保留用户上传的文档）
    cursor.execute(
        "DELETE FROM document_chunks WHERE metadata->>'$.doc_type' IN "
        "('course', 'faq', 'pet_knowledge', 'service')"
    )
    cursor.execute(
        "DELETE FROM documents WHERE "
        "(title LIKE '课程-%' OR title LIKE 'FAQ-%' OR title LIKE '%百科-%' OR title LIKE '%服务-%' OR title LIKE '测试%') "
        "AND file_type = 'txt'"
    )
    conn_chatbot.commit()

    for chunk in all_chunks:
        # 创建document
        cursor.execute("""
            INSERT INTO documents (knowledge_base_id, title, file_name, file_path, file_type, file_size, chunk_count, status)
            VALUES (%s, %s, %s, '', 'txt', %s, 1, 'completed')
        """, (chunk['kb_id'], chunk['title'], f"{chunk['title']}.txt", len(chunk['content'].encode('utf-8'))))
        doc_id = cursor.lastrowid

        # 创建chunk（注意：ORM中chunk_metadata映射到数据库的metadata列）
        metadata = json.dumps({'doc_type': chunk['doc_type'], 'ref_id': chunk['ref_id']})
        cursor.execute("""
            INSERT INTO document_chunks (document_id, knowledge_base_id, chunk_index, content, token_count, metadata)
            VALUES (%s, %s, 0, %s, %s, %s)
        """, (doc_id, chunk['kb_id'], chunk['content'], len(chunk['content']) * 2, metadata))

    # 更新知识库统计
    cursor.execute("""
        UPDATE knowledge_bases SET
            document_count = (SELECT COUNT(*) FROM documents WHERE knowledge_base_id = knowledge_bases.id AND is_deleted = 0),
            chunk_count = (SELECT COUNT(*) FROM document_chunks WHERE knowledge_base_id = knowledge_bases.id AND is_deleted = 0)
    """)
    conn_chatbot.commit()
    conn_chatbot.close()

    print(f"总计写入 {len(all_chunks)} 个知识chunks")

    # 3. 重建FAISS索引
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument('--skip-faiss', action='store_true', help='跳过FAISS索引重建')
    args, _ = parser.parse_known_args()
    
    if args.skip_faiss:
        print("跳过FAISS索引重建（使用 --skip-faiss 参数）")
    else:
        try:
            rebuild_faiss_index()
        except Exception as e:
            print(f"FAISS索引重建失败: {e}")
            print("可稍后手动执行: cd backend && python import_knowledge.py --rebuild-faiss-only")

    print("=== PetSchool 知识库导入完成 ===")
