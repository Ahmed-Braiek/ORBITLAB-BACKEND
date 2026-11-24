param([string]$BaseUrl="http://localhost:8080")
$ErrorActionPreference="Stop"
$favoriteId=$null
$listId=$null
try {
 Write-Host "OrbitLab Phase 8 local REST smoke test"
 $health=Invoke-RestMethod -Uri "$BaseUrl/api/health"
 if($health.status -ne "UP"){throw "Health check failed"}
 $planets=Invoke-RestMethod -Uri "$BaseUrl/api/planets"
 if(@($planets).Count -ne 8){throw "Expected 8 planets"}
 $suffix=[guid]::NewGuid().ToString("N").Substring(0,8)
 $objId="demo-$suffix"
 $fav=Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/observatory/favorites" -ContentType "application/json" -Body (@{objectType="asteroid";objectId=$objId;displayName="Demo object $suffix"}|ConvertTo-Json)
 $favoriteId=$fav.id
 if(-not $favoriteId){throw "Favorite creation failed"}
 $favorites=Invoke-RestMethod -Uri "$BaseUrl/api/observatory/favorites"
 if(-not @($favorites|Where-Object {$_.id -eq $favoriteId}).Count){throw "Favorite not returned"}
 $list=Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/observatory/lists" -ContentType "application/json" -Body (@{name="SOA smoke $suffix";description="Temporary test list"}|ConvertTo-Json)
 $listId=$list.id
 if(-not $listId){throw "List creation failed"}
 $entry=Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/observatory/lists/$listId/entries" -ContentType "application/json" -Body (@{objectType="planet";objectId="mars";displayName="Mars";note="Smoke test"}|ConvertTo-Json)
 if(-not $entry.id){throw "Entry creation failed"}
 $entries=Invoke-RestMethod -Uri "$BaseUrl/api/observatory/lists/$listId/entries"
 if(-not @($entries|Where-Object {$_.id -eq $entry.id}).Count){throw "Entry not returned"}
 Invoke-RestMethod -Method Delete -Uri "$BaseUrl/api/observatory/lists/$listId/entries/$($entry.id)" | Out-Null
 Write-Host "PASS: Health, 8 planets, favorites CRUD, lists CRUD, list entries CRUD."
 Write-Host "NASA/JPL external calls not tested by this smoke script."
}catch{
 Write-Error "FAIL: $($_.Exception.Message)"
 exit 1
}finally{
 if($listId){try{Invoke-RestMethod -Method Delete -Uri "$BaseUrl/api/observatory/lists/$listId"|Out-Null}catch{Write-Warning "Could not remove test list $listId"}}
 if($favoriteId){try{Invoke-RestMethod -Method Delete -Uri "$BaseUrl/api/observatory/favorites/$favoriteId"|Out-Null}catch{Write-Warning "Could not remove test favorite $favoriteId"}}
}
