# Authentication

The booking system uses [ORY Kratos](https://www.ory.com/docs/kratos) for identity management. Kratos runs on a suburl (`/services/identity/...`).
They provide two main authentication mechanisms:
- E-Mail/Password
- OAuth2 (from university or so)

It is possible to get user details (name, id, email) from the `/services/identity/sessions/whoami` endpoint.

## Cookie

The website uses cookie based authentication (`ory-session`). Simply open the webbrowser (`/auth/login`) and wait until the cookie is set on the same origin.
This is simple, but requires a potential **insecure** webview and cookie sniffing.

## Native/App
The native authentication is well documented [here](https://www.ory.com/docs/kratos/social-signin/native-apps) and [here](https://www.ory.com/docs/kratos/self-service/flows/user-login).
The general procedure is simple, but puts a lot of logic into the app itself:
1. Create login flow (`POST /services/identity/self-service/login/api?return_to=uninow://COURSE/login&return_session_token_exchange_code=true`)
2. Show UI to the user (with all saml/oidc options and username/password)
3. POST json data (e.g. `{"provider": "saml-lmu"}` or username/password) to the action url
4. Get a 422 error with the field `redirect_browser_to`
5. Open custom tab/browser with that url
6. Handle redirect (`uninow://COURSE/login?code=abc`)
7. Exchange code for session token

## OAuth
They apparently don't use Hydra, so proper OAuth is not possible. Sadly.
