#!/bin/bash

# ==========================================
# AWS 리소스 비용 절감 스크립트 (퇴근용)
# ==========================================

CLUSTER_NAME="rush_deal_msa"
DB_INSTANCE_ID="postgres" # RDS 인스턴스 식별자
REGION="ap-northeast-2"

echo "🛑 AWS 리소스 정리를 시작합니다..."

# 1. ECS 서비스 개수 0으로 줄이기 (Fargate 비용 0원 만들기)
# 서비스 목록: user, order, payment, product, queue, timedeal (auth 포함)
SERVICES=("user-service" "order-service" "payment-service" "product-service" "queue-service" "timedeal-service" "auth-service")

for SERVICE in "${SERVICES[@]}"
do
    echo "   [ECS] Stopping tasks for $SERVICE..."
    # Auto Scaling이 다시 늘리는 걸 막기 위해 Min/Max도 0으로 수정 (선택사항이나 안전함)
    # 단순히 Desired Count만 0으로 하면 Auto Scaling이 다시 1로 늘릴 수 있음!
    aws application-autoscaling register-scalable-target \
        --service-namespace ecs \
        --resource-id service/$CLUSTER_NAME/$SERVICE \
        --scalable-dimension ecs:service:DesiredCount \
        --min-capacity 0 \
        --max-capacity 0 \
        --region $REGION > /dev/null 2>&1

    aws ecs update-service \
        --cluster $CLUSTER_NAME \
        --service $SERVICE \
        --desired-count 0 \
        --region $REGION > /dev/null
done
echo "✅ ECS 모든 태스크 종료 완료 (비용 발생 중단)"

# 2. RDS 데이터베이스 일시 정지 (Stop)
# 주의: 최대 7일간만 정지됨. 그 이후엔 자동으로 다시 켜짐.
echo "   [RDS] Stopping Database ($DB_INSTANCE_ID)..."
aws rds stop-db-instance \
    --db-instance-identifier $DB_INSTANCE_ID \
    --region $REGION > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "✅ RDS 정지 명령 전송 완료 (완전히 멈추는데 몇 분 걸림)"
else
    echo "⚠️ RDS 정지 실패 (이미 꺼져있거나 이름이 틀림)"
fi

# 3. NAT Gateway 경고 (스크립트로 지우긴 위험함)
echo "--------------------------------------------------------"
echo "⚠️ [중요] NAT Gateway와 MSK는 '일시 정지'가 불가능합니다!"
echo "   비용이 걱정된다면 AWS 콘솔에서 직접 '삭제(Delete)' 하세요."
echo "   (NAT Gateway는 시간당 약 60원이 계속 나갑니다)"
echo "--------------------------------------------------------"
