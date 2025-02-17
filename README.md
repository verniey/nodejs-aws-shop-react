# Task 2

---

## **Manual Deployment (Task 2.1)**

### Deployment Overview
- **S3 Bucket:** `myshop-app-1708123456`
- **CloudFront Distribution ID:** `E2761ZXVQ1YREK`
- **S3 Website URL (Expected: 403 Forbidden)**  
  [http://myshop-app-1708123456.s3-website.eu-central-1.amazonaws.com](http://myshop-app-1708123456.s3-website.eu-central-1.amazonaws.com)
- **CloudFront URL (Application Available)**  
  [https://d3a09zodkwe899.cloudfront.net](https://d3a09zodkwe899.cloudfront.net)

---

### 1. Create & Configure S3 Bucket
1. **Create a new bucket** in AWS S3:
   - **Bucket Name:** `myshop-app-1708123456`
   - **Region:** `eu-central-1`
   - **Enable Static Website Hosting**
2. **Set S3 Bucket Policy**:
   ```json
   {
       "Version": "2012-10-17",
       "Statement": [
           {
               "Sid": "PublicReadGetObject",
               "Effect": "Allow",
               "Principal": "*",
               "Action": "s3:GetObject",
               "Resource": "arn:aws:s3:::myshop-app-1708123456/*"
           }
       ]
   }
   ```
3. **Upload `build/` files to S3**:

---

### 2. Create CloudFront Distribution
1. **Create a CloudFront Distribution**:
    - **Origin:** `myshop-app-1708123456.s3.amazonaws.com`
    - **Viewer Protocol Policy:** Redirect HTTP to HTTPS
    - **Cache Policy:** `CachingOptimized`
2. **Update CloudFront Policy**:
   
---

### 3. Verify Deployment. Examples
- **CloudFront Access:**
  ```
  https://d3a09zodkwe899.cloudfront.net
  ```
- **S3 URL Returns 403 Forbidden:**
  ```
  http://myshop-app-1708123456.s3-website.eu-central-1.amazonaws.com
  ```
---

### 4. Modify & Deploy Updates
1. **Modify `PageProducts.tsx`**:
   ```tsx
   <Typography variant="h4" align="center" color="secondary">
       🚀 MyShop Updated Version - Now on CloudFront 🎉
   </Typography>
   ```
2. **Rebuild & Upload Changes to S3**:
3. **Invalidate CloudFront Cache**:
    - Open CloudFront → Click **Invalidations** → Enter:
      ```
      /*
      ```

---

## Final Checklist Task 2.1
| Task                                   | Status |
|----------------------------------------|--------|
| **S3 Bucket Created & Configured**     | ✅ Done |
| **App Uploaded & Accessible via S3**   | ✅ Done |
| **CloudFront Configured & Serving App**| ✅ Done |
| **S3 URL Returns 403 Forbidden**       | ✅ Done |
| **Changes Successfully Deployed**      | ✅ Done |


### Destroy the created AWS infrastructure. Check deletions
```
aws cloudfront list-distributions
aws s3 ls 
````
                       

## Tasks 2.2 & 2.3: Automated Deployment with AWS CDK

### Deployment URLs examples
- **S3 Website URL:** http://myshopcdkstack-myshopbucket3363d19f-zsmgs117aslt.s3-website.eu-central-1.amazonaws.com/
- **CloudFront URL:** https://dnytp1yiefjzc.cloudfront.net/

---
### AWS CDK Bootstrap

You only need to run `cdk bootstrap` once per AWS account and region before deploying stacks with AWS CDK. If you have already bootstrapped your environment before, you do not need to run it again.


### Command to Run Bootstrap (if needed)
```
cdk bootstrap
```

### **1️⃣ Destroy Existing AWS Resources (if needed)**
Run from **`myshop-cdk`** directory:
```
cdk destroy
```
This will remove the S3 bucket, CloudFront distribution, and related resources.

---

### **2️⃣ Deploy the Infrastructure**
Run from **`myshop-cdk`** directory:
```
cdk deploy
```
✅ This command will:
- Create an **S3 bucket** with static website hosting.
- Set up **CloudFront** for global content distribution.
- Configure **bucket policies** to ensure CloudFront can access S3.
- Deploy the latest application files.

---

### **3️⃣ Deploy Application to S3 and Invalidate CloudFront Cache**
Run from **`nodejs-aws-shop-react`** directory:
```
npm run deploy
```
✅ This script will:
- Build the application.
- Sync the `dist/` folder to the S3 bucket.
- Invalidate the CloudFront cache to reflect new changes.

---

### **4️⃣ Verify Deployment**
### **Example: Verify Deployment**

#### **Check CloudFront Distribution**
Run:
```
curl -I https://dnytp1yiefjzc.cloudfront.net
```
Expected Response: `HTTP/2 200 OK`

#### **Check S3 Bucket URL (Should Return 403 Forbidden)**
Run:
```
curl -I http://myshopcdkstack-myshopbucket3363d19f-zsmgs117aslt.s3-website.eu-central-1.amazonaws.com
```
Expected Response: `HTTP/1.1 403 Forbidden`

---


### **Final Checklist**
| Check | Status |
|------------------------------|--------|
| **S3 Bucket returns 403 Forbidden** | ✅ Done |
| **CloudFront URL serves app correctly** | ✅ Done |
| **CDK deploys infrastructure without manual changes** | ✅ Done |
| **Application deployed using npm run deploy** | ✅ Done |
| **Pull Request created and submitted** | ✅ Done |

---

