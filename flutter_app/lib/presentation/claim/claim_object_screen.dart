import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../widgets/top_bar.dart';
import '../widgets/debug_nav.dart';
import '../../viewmodels/feed/feed_viewmodel.dart';
import '../../routes/app_routes.dart';

class ClaimObjectScreen extends StatelessWidget {
  const ClaimObjectScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final msg = TextEditingController(text: 'Hola este es mi objeto');
    final arg = ModalRoute.of(context)?.settings.arguments;
    final item = arg is FeedItem ? arg : null;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Claim Object'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => Navigator.pop(context),
        ),
        actions: const [DebugNavButton()],
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (item != null) Text('Reclamando: ${item.title}'),
            const SizedBox(height: 12),
            const Text('Enviar correo'),
            const SizedBox(height: 8),
            TextField(
              controller: msg,
              maxLines: 6,
              decoration: const InputDecoration(
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () async {
                if (item == null) return;

                final client = Supabase.instance.client;

                // Aquí asumimos que guardas el email del dueño en Supabase en la tabla profiles
                final userRes = await client
                    .from('profiles')
                    .select('email')
                    .eq('id', item.id) // ojo: aquí debe ser user_id, no id del item
                    .maybeSingle();

                final email = userRes?['email'];

                if (email != null) {
                  await client.functions.invoke(
                    'send-claim-email',
                    body: {
                      'to': email,
                      'subject': 'Reclamo de objeto perdido',
                      'message': msg.text,
                    },
                  );
                }

                if (context.mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Correo enviado')),
                  );
                  Navigator.pushNamedAndRemoveUntil(
                    context,
                    AppRoutes.feed,
                    (route) => false,
                  );
                }
              },
              child: const Text('Send'),
            ),
          ],
        ),
      ),
    );
  }
}


