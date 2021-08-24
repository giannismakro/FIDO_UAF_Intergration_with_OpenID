# Keycloak Version 13.0.0

## Configure and Deploy instructions for Keycloak 

### These instructions have been tested on Ubuntu 20.04.02

#### Install and Run the Keycloak server :
 
 Download and unzip the keycloak server.
 
 Open the terminal and go to the bin folder on the Keycloak's directory
 ```shell
cd /path-to/keycloak-13.0.0/bin
```

Run the keycloak server:
```shell
./standalone.sh -Djboss.socket.binding.port-offset=100 -b=0.0.0.0 -bmanagement=0.0.0.0
```

`-Djboss.socket.binding.port-offset=100`: This value is added to the base value of every port opened by the Keycloak server. In this example, 100 is the value.So our keycloak runs
on 8180 port

`-b=0.0.0.0`,`-bmanagement=0.0.0.0`:These parameters enable remote access from any source (IP/hostname) to the administrative page.


#### Create an administrator

From the browser access Keycloak Admin console at http://localhost:8180/auth/admin, create an Admin account and authenticate using the following credentials:
   
```
Username  | Admin
Password  | Admin
```
#### Creating a realm

From the **Master menu** , click **Add Realm** . When you are logged in to the master realm, this menu lists all other realms.

Type **demo** in the **Name** field.

Click **Create**.
#### Creating an administrator for a specific realm

In the **demo** realm, you create a new Administrator user and a  password for that new admin.

Procedure
1. From the menu, click **Users** to open the user list page.

2. On the right side of the empty user list, click **Add User** to open the Add user page.

3. Enter a name in the **Username** field.

This is the only required field.

Click the **Credentials** tab to set  password for the new user.

Disable **Temporary**

Type a new password and confirm it.

Click **Set Password** to set the user password to the new one you specified.

Click **Role Mappings**

On the **Client Roles** rollbar select **realm-management**

On the **Available roles** select **realm-admin** to go to the assigned roles.

#### Creating a User

In the **demo** realm, you create a new user and a  password for that new user.

Procedure
1. From the menu, click **Users** to open the user list page.
 
2. On the right side of the empty user list, click **Add User** to open the Add user page.

3. Enter a name in the **Username** field.

This is the only required field.

Click the **Credentials** tab to set  password for the new user.

Disable **Temporary**

Type a new password and confirm it.

Click **Set Password** to set the user password to the new one you specified.

Click **Attribures** to set the user fidoAuthenticationId attribute.

On **key** section add the value **fidoAuthenticationId**  and set a random value on **Value** section.

Click **Add** under **Actions** and then **Save**.