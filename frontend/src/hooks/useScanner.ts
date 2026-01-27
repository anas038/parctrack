import { useEffect, useRef, useState } from 'react'
import { Html5Qrcode } from 'html5-qrcode'

interface UseScannerOptions {
  onScan: (code: string) => void
  onError?: (error: string) => void
}

export function useScanner({ onScan, onError }: UseScannerOptions) {
  const [isScanning, setIsScanning] = useState(false)
  const [hasCamera, setHasCamera] = useState(true)
  const scannerRef = useRef<Html5Qrcode | null>(null)
  const elementId = 'qr-reader'

  const startScanning = async () => {
    if (scannerRef.current) {
      return
    }

    try {
      const scanner = new Html5Qrcode(elementId)
      scannerRef.current = scanner

      await scanner.start(
        { facingMode: 'environment' },
        {
          fps: 10,
          qrbox: { width: 250, height: 250 },
        },
        (decodedText) => {
          onScan(decodedText)
        },
        () => {
          // Ignore errors during scanning
        }
      )
      setIsScanning(true)
    } catch (err) {
      setHasCamera(false)
      onError?.('Camera not available')
    }
  }

  const stopScanning = async () => {
    if (scannerRef.current) {
      try {
        await scannerRef.current.stop()
        scannerRef.current.clear()
      } catch {
        // Ignore errors when stopping
      }
      scannerRef.current = null
      setIsScanning(false)
    }
  }

  useEffect(() => {
    return () => {
      stopScanning()
    }
  }, [])

  return {
    elementId,
    isScanning,
    hasCamera,
    startScanning,
    stopScanning,
  }
}
