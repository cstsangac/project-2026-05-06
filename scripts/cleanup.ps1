param(
  [switch]$PruneBuilderCache,
  [switch]$PruneSystem,
  [switch]$RemoveVolumes
)

$ErrorActionPreference = "Stop"

function Info([string]$msg) { Write-Host "[cleanup] $msg" }

Info "Stopping compose stack..."
if ($RemoveVolumes) {
  Info "Removing containers + volumes (data will be deleted)"
  docker compose down -v | Out-Host
} else {
  docker compose down | Out-Host
}

if ($PruneBuilderCache) {
  Info "Pruning build cache..."
  docker builder prune -f | Out-Host
}

if ($PruneSystem) {
  Info "Pruning unused images/containers/networks..."
  docker system prune -f | Out-Host
}

Info "Done."

