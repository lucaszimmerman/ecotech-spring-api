param(
    [ValidateRange(1024, 65535)]
    [int]$Port = 8081
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$reviewKeyBytes = New-Object byte[] 32
$random = [System.Security.Cryptography.RandomNumberGenerator]::Create()
try {
    $random.GetBytes($reviewKeyBytes)
} finally {
    $random.Dispose()
}
$reviewKey = [Convert]::ToBase64String($reviewKeyBytes)
$reviewEnvironment = @{
    AWS_EC2_METADATA_DISABLED = 'true'
    AWS_ACCESS_KEY_ID = 'review-only'
    AWS_SECRET_ACCESS_KEY = 'review-only'
    AWS_SESSION_TOKEN = $null
}
$previousEnvironment = @{}

Push-Location $projectRoot
try {
    foreach ($name in $reviewEnvironment.Keys) {
        $previousEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
        [Environment]::SetEnvironmentVariable($name, $reviewEnvironment[$name], 'Process')
    }

    Write-Host "Review Swagger: http://localhost:$Port/swagger-ui/index.html"
    Write-Host 'Temporary H2 database; .env is ignored. Email, images and Google require real integrations.'

    & .\mvnw.cmd --batch-mode --no-transfer-progress test-compile spring-boot:run `
        '-Dspring-boot.run.useTestClasspath=true' `
        '-Dspring-boot.run.profiles=test' `
        "-Dspring-boot.run.arguments=--server.address=127.0.0.1 --server.port=$Port --spring.config.import=optional:classpath:review-no-env.properties --security.jwt.secret-key=$reviewKey --spring.devtools.restart.enabled=false"

    if ($LASTEXITCODE -ne 0) {
        throw "Review server exited with code $LASTEXITCODE."
    }
} finally {
    foreach ($name in $previousEnvironment.Keys) {
        [Environment]::SetEnvironmentVariable($name, $previousEnvironment[$name], 'Process')
    }
    Pop-Location
}
