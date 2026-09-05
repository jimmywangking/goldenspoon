<template>
  <div ref="containerRef" class="scene-container"></div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, defineProps, defineEmits } from 'vue'
import * as THREE from 'three'

// @ts-ignore - three.js ESM modules typed separately
const OrbitControls = (THREE as any).OrbitControls

interface ModuleConfig {
  id: string
  name: string
  position: { x: number; y: number; z: number }
  rotation: { x: number; y: number; z: number }
  scale: { x: number; y: number; z: number }
  color: string
}

const props = defineProps<{
  modules: ModuleConfig[]
  selectedId: string
  readonly: boolean
}>()

const emit = defineEmits<{
  (e: 'select', id: string): void
}>()

const containerRef = ref<HTMLDivElement>()
let scene: THREE.Scene | null = null
let camera: THREE.PerspectiveCamera | null = null
let renderer: THREE.WebGLRenderer | null = null
let controls: any = null
let raycaster = new THREE.Raycaster()
let mouse = new THREE.Vector2()
let selectedMesh: THREE.Mesh | null = null
let moduleMeshes = new Map<string, THREE.Mesh>()

function init() {
  if (!containerRef.value) return

  scene = new THREE.Scene()
  scene.background = new THREE.Color(0xf0f2f5)

  camera = new THREE.PerspectiveCamera(
    50,
    containerRef.value.clientWidth / containerRef.value.clientHeight,
    0.1,
    1000
  )
  camera.position.set(15, 12, 15)
  camera.lookAt(0, 0, 0)

  renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setSize(containerRef.value.clientWidth, containerRef.value.clientHeight)
  renderer.setPixelRatio(window.devicePixelRatio)
  renderer.shadowMap.enabled = true
  containerRef.value.appendChild(renderer.domElement)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.05

  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
  scene.add(ambientLight)

  const dirLight = new THREE.DirectionalLight(0xffffff, 0.8)
  dirLight.position.set(10, 20, 10)
  dirLight.castShadow = true
  dirLight.shadow.mapSize.width = 2048
  dirLight.shadow.mapSize.height = 2048
  scene.add(dirLight)

  const gridHelper = new THREE.GridHelper(20, 20, 0x888888, 0xcccccc)
  scene.add(gridHelper)

  const floorGeometry = new THREE.PlaneGeometry(20, 20)
  const floorMaterial = new THREE.MeshStandardMaterial({ color: 0xeeeeee, roughness: 0.8 })
  const floor = new THREE.Mesh(floorGeometry, floorMaterial)
  floor.rotation.x = -Math.PI / 2
  floor.position.y = -0.01
  floor.receiveShadow = true
  scene.add(floor)

  function animate() {
    requestAnimationFrame(animate)
    if (controls) controls.update()
    if (renderer && scene && camera) renderer.render(scene, camera)
  }
  animate()

  const handleClick = (event: MouseEvent) => {
    if (props.readonly) return
    if (!renderer || !camera) return
    const rect = renderer.domElement.getBoundingClientRect()
    mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
    mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1

    raycaster.setFromCamera(mouse, camera)
    const intersects = raycaster.intersectObjects(Array.from(moduleMeshes.values()))

    if (intersects.length > 0) {
      const mesh = intersects[0].object as THREE.Mesh
      emit('select', mesh.userData.id as string)
    } else {
      emit('select', '')
    }
  }
  renderer.domElement.addEventListener('click', handleClick)

  const handleResize = () => {
    if (!containerRef.value || !camera || !renderer) return
    const width = containerRef.value.clientWidth
    const height = containerRef.value.clientHeight
    camera.aspect = width / height
    camera.updateProjectionMatrix()
    renderer.setSize(width, height)
  }
  window.addEventListener('resize', handleResize)
}

function rebuildModules() {
  if (!scene) return

  for (const [, mesh] of moduleMeshes) {
    scene.remove(mesh)
    mesh.geometry.dispose()
    ;(mesh.material as THREE.Material).dispose()
  }
  moduleMeshes.clear()
  selectedMesh = null

  for (const mod of props.modules) {
    const geometry = new THREE.BoxGeometry(1, 1, 1)
    const material = new THREE.MeshStandardMaterial({
      color: new THREE.Color(mod.color),
      roughness: 0.5,
      metalness: 0.1
    })
    const mesh = new THREE.Mesh(geometry, material)
    mesh.position.set(mod.position.x, mod.position.y + 0.5, mod.position.z)
    mesh.rotation.set(
      mod.rotation.x * Math.PI / 180,
      mod.rotation.y * Math.PI / 180,
      mod.rotation.z * Math.PI / 180
    )
    mesh.scale.set(mod.scale.x, mod.scale.y, mod.scale.z)
    mesh.castShadow = true
    mesh.receiveShadow = true
    mesh.userData.id = mod.id
    mesh.userData.name = mod.name
    scene.add(mesh)
    moduleMeshes.set(mod.id, mesh)
  }

  if (props.selectedId && moduleMeshes.has(props.selectedId)) {
    highlightMesh(moduleMeshes.get(props.selectedId)!)
  }
}

function highlightMesh(mesh: THREE.Mesh) {
  if (selectedMesh) {
    ;(selectedMesh.material as THREE.MeshStandardMaterial).emissive.setHex(0x000000)
  }
  selectedMesh = mesh
  ;(mesh.material as THREE.MeshStandardMaterial).emissive.setHex(0x333333)
}

watch(() => props.modules, rebuildModules, { deep: true })
watch(() => props.selectedId, (newId) => {
  if (!scene) return
  if (selectedMesh) {
    ;(selectedMesh.material as THREE.MeshStandardMaterial).emissive.setHex(0x000000)
    selectedMesh = null
  }
  if (newId && moduleMeshes.has(newId)) {
    highlightMesh(moduleMeshes.get(newId)!)
  }
})

onMounted(init)
</script>

<style scoped>
.scene-container {
  width: 100%;
  height: 100%;
  min-height: 400px;
}
</style>
