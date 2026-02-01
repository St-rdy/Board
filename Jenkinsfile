// @Library('ci-framework@main') _ 

pipeline {
    agent { kubernetes { cloud 'OpenShift' } }
    
    triggers {
        genericTrigger(
            genericVariables: [
                [key: 'REPO_NAME', value: '$.repository.full_name'],
                [key: 'PR_NUMBER', value: '$.pull_request.number'],
                [key: 'PR_ACTION', value: '$.action'],
                [key: 'PR_MERGED', value: '$.pull_request.merged'],
                [key: 'COMMENT_BODY', value: '$.comment.body'],
                [key: 'SENDER', value: '$.sender.login']
            ],
            tokenCredentialId: 'stardy-pipeline',
            regexpFilterText: '$PR_ACTION:$COMMENT_BODY:$PR_MERGED',
            regexpFilterExpression: '^(opened:.*:.*|edited:.*:.*|reopened:.*:.*|created:.*#RUN_CT.*:.*|closed:.*:true)$',
            causeString: 'Triggered on $REPO_NAME'
        )
    }

    stages {
        stage('Debug Payload') {
            steps {
                script {
                    // 값 확인
                    echo "--- GWT Variable Check ---"
                    echo "입수된 레포지토리명: ${env.REPO_NAME}"
                    echo "입수된 PR 번호: ${env.PR_NUMBER}"
                    echo "입수된 액션: ${env.PR_ACTION}"
                    echo "입수된 댓글 내용: ${env.COMMENT_BODY}"
                    echo "전송자: ${env.SENDER}"
                    echo "--------------------------"
                }
            }
        }
    }
}