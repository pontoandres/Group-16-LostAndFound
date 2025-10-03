// lib/presentation/lost_report/lost_report_screen.dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../viewmodels/lost_report/lost_report_viewmodel.dart';

class LostReportScreen extends StatelessWidget {
  const LostReportScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => LostReportViewModel(),
      child: const _Form(),
    );
  }
}

class _Form extends StatelessWidget {
  const _Form();

  @override
Widget build(BuildContext context) {
  final vm = context.watch<LostReportViewModel>();

  return Scaffold(
    appBar: AppBar(title: const Text('Report a lost item')),
    body: SafeArea(
      // ✅ ListView da constraints y evita el “not laid out”
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Preview de la imagen (con alto fijo)
          if (vm.imageBytes != null)
            ClipRRect(
              borderRadius: BorderRadius.circular(8),
              child: Image.memory(
                vm.imageBytes!,
                height: 180,
                width: double.infinity,
                fit: BoxFit.cover,
              ),
            )
          else
            OutlinedButton.icon(
              onPressed: () => vm.pickImage(),
              icon: const Icon(Icons.photo),
              label: const Text('Choose image'),
            ),
          const SizedBox(height: 16),

          TextField(
            controller: vm.titleCtrl,
            decoration: const InputDecoration(labelText: 'Title'),
          ),
          const SizedBox(height: 12),

          TextField(
            controller: vm.descriptionCtrl,
            maxLines: 3,
            decoration: const InputDecoration(labelText: 'Description'),
          ),
          const SizedBox(height: 12),

          TextField(
            controller: vm.locationCtrl,
            decoration: const InputDecoration(labelText: 'Location'),
          ),
          const SizedBox(height: 12),

          // Fecha
          Row(
            children: [
              Expanded(
                child: Text('Lost at: ${vm.lostAt?.toLocal() ?? '-'}'),
              ),
              TextButton(
                onPressed: () async {
                  final now = DateTime.now();
                  final picked = await showDatePicker(
                    context: context,
                    initialDate: vm.lostAt ?? now,
                    firstDate: DateTime(now.year - 3),
                    lastDate: DateTime(now.year + 3),
                  );
                  if (picked != null) vm.setLostAt(picked);
                },
                child: const Text('Pick date'),
              ),
            ],
          ), 
          const SizedBox(height: 24),

          // Guardar
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: vm.isLoading
                  ? null
                  : () async {
                      final ok = await vm.submit();
                      if (!context.mounted) return;
                      if (ok) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(content: Text('Item reported!')),
                        );
                        Navigator.pop(context); // vuelve al feed
                      } else {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(content: Text(vm.error ?? 'Error')),
                        );
                      }
                    },
              child: vm.isLoading
                  ? const CircularProgressIndicator()
                  : const Text('Report'),
            ),
          ),
          const SizedBox(height: 24),
        ],
      ),
    ),
  );
}
}