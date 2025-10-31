import 'package:connectivity_plus/connectivity_plus.dart';

/// Servicio global para monitorear cambios de conectividad.
/// Emite un stream cada vez que el dispositivo cambia de estado (wifi, móvil, sin conexión).
class NetworkMonitor {
  final Connectivity _connectivity = Connectivity();

  /// Devuelve un Stream que emite el estado actual de la red (como lista).
  Stream<List<ConnectivityResult>> get onConnectivityChanged =>
      _connectivity.onConnectivityChanged;
}
