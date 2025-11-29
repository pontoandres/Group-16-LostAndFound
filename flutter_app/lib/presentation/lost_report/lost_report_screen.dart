import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../viewmodels/lost_report/lost_report_viewmodel.dart';
import 'package:image_picker/image_picker.dart';
// Los siguientes imports ya no se usan, pero puedes dejarlos o borrarlos
// import 'package:shared_preferences/shared_preferences.dart';
// import 'dart:convert';

class LostReportScreen extends StatelessWidget {
  const LostReportScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => LostReportViewModel(),
      child: const _LostReportBody(),
    );
  }
}

class _LostReportBody extends StatelessWidget {
  const _LostReportBody();

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<LostReportViewModel>();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Report lost item'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // ---------------- TÍTULO + BOTÓN SMART TITLE ------------------
            Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: vm.titleCtrl,
                    decoration: const InputDecoration(
                      labelText: 'Title',
                      filled: true,
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                IconButton(
                  tooltip: 'Suggest title',
                  onPressed:
                      vm.isLoading ? null : () => vm.suggestSmartTitle(),
                  icon: const Icon(Icons.auto_fix_high),
                ),
              ],
            ),
            const SizedBox(height: 12),

            // ---------------- DESCRIPCIÓN + CONTADOR ----------------------
            TextField(
              controller: vm.descriptionCtrl,
              maxLines: 3,
              decoration: const InputDecoration(
                labelText: 'Description',
                filled: true,
              ),
            ),
            const SizedBox(height: 4),
            Align(
              alignment: Alignment.centerRight,
              child: StreamBuilder<int>(
                stream: vm.descRemainingStream,
                initialData:
                    300 - vm.descriptionCtrl.text.characters.length,
                builder: (context, snapshot) {
                  final remaining = snapshot.data ?? 0;
                  return Text(
                    '$remaining characters remaining',
                    style: const TextStyle(
                      fontSize: 12,
                      color: Colors.grey,
                    ),
                  );
                },
              ),
            ),
            const SizedBox(height: 12),

            TextField(
              controller: vm.categoryCtrl,
              decoration: const InputDecoration(
                labelText: 'Category',
                filled: true,
                hintText: 'e.g. wallet, keys, phone...',
              ),
            ),
            const SizedBox(height: 12),

            TextField(
              controller: vm.locationCtrl,
              decoration: const InputDecoration(
                labelText: 'Location',
                filled: true,
              ),
            ),
            const SizedBox(height: 12),

            Row(
              children: [
                Expanded(
                  child: Text(
                    vm.lostAt != null
                        ? vm.lostAt!.toLocal().toString()
                        : '',
                  ),
                ),
                TextButton(
                  onPressed: () async {
                    final now = DateTime.now();
                    final picked = await showDatePicker(
                      context: context,
                      initialDate: vm.lostAt ?? now,
                      firstDate: DateTime(now.year - 3),
                      lastDate: DateTime(now.year + 1),
                    );
                    if (picked != null) {
                      final time = await showTimePicker(
                        context: context,
                        initialTime:
                            TimeOfDay.fromDateTime(vm.lostAt ?? now),
                      );
                      final dt = DateTime(
                        picked.year,
                        picked.month,
                        picked.day,
                        time?.hour ?? 0,
                        time?.minute ?? 0,
                      );
                      vm.setLostAt(dt);
                    }
                  },
                  child: const Text('Pick date/time'),
                ),
              ],
            ),
            const SizedBox(height: 12),

            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: vm.isLoading
                        ? null
                        : () =>
                            vm.pickImage(source: ImageSource.gallery),
                    icon: const Icon(Icons.photo),
                    label: const Text('Gallery'),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: vm.isLoading
                        ? null
                        : () =>
                            vm.pickImage(source: ImageSource.camera),
                    icon: const Icon(Icons.camera_alt),
                    label: const Text('Camera'),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),

            if (vm.imageBytes != null)
              ClipRRect(
                borderRadius: BorderRadius.circular(8),
                child: Image.memory(
                  vm.imageBytes!,
                  height: 160,
                  fit: BoxFit.cover,
                ),
              ),
            const SizedBox(height: 20),

            SizedBox(
              height: 52,
              width: double.infinity,
              child: ElevatedButton(
                onPressed: vm.isLoading
                    ? null
                    : () async {
                        final ok = await vm.submit(context);
                        if (ok && context.mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                              content: Text('Item reported'),
                            ),
                          );
                          Navigator.pop(context, true);
                        } else if (vm.error != null && context.mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(content: Text(vm.error!)),
                          );
                        }
                      },
                child: vm.isLoading
                    ? const CircularProgressIndicator(
                        color: Colors.white,
                      )
                    : const Text('Submit'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}


